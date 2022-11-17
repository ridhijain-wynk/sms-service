package in.wynk.sms.core.service;

import in.wynk.advice.TimeIt;
import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.repository.MessagesRepository;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.utils.DiffMatchPatch;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static in.wynk.common.constant.BaseConstants.SMS_MESSAGE_TEMPLATE_CONTEXT;
import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;
import static in.wynk.sms.constants.SMSConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.LUCENE_FIND_SMS_ERROR;
import static in.wynk.sms.constants.SmsLoggingMarkers.TIME_TAKEN_TO_FIND_SMS;

@Slf4j
@RequiredArgsConstructor
@Primary
@Service
public class MessageServiceV2 implements IMessageService{

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final MessagesRepository messagesRepository;
    private final MessageCachingService messageCachingService;

    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private final Directory index = new RAMDirectory();
    private static final int LUCENE_HITS_PER_PAGE = 30;
    private static final int LUCENE_WINDOW_SIZE = 5;
    private static final int LUCENE_OUTPUT_WINDOW = 5;

    @PostConstruct
    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
    public void init() {
        loadMessageIndices();
    }

    @SneakyThrows
    public void loadMessageIndices(){
        final Collection<Messages> allMessages = messagesRepository.getMessagesByState(State.ACTIVE);
        if (CollectionUtils.isNotEmpty(allMessages) && writeLock.tryLock()) {
            try {
                final IndexWriterConfig config = new IndexWriterConfig(analyzer);
                final IndexWriter writer = new IndexWriter(index, config);

                writer.deleteAll();
                for(Messages message : allMessages) {
                    final Document document = new Document();
                    document.add(new StringField(ID, message.getId(), Field.Store.YES));
                    document.add(new TextField(MESSAGE_TEXT, message.getMessage(), Field.Store.YES));
                    writer.addDocument(document);
                }
                writer.close();
            } catch (Throwable th) {
                log.error(APPLICATION_ERROR, "Exception occurred while refreshing messages cache. Exception: {}", th.getMessage(), th);
                throw th;
            } finally {
                writeLock.unlock();
            }
        }
    }

    @TimeIt
    private List<String> getMessagesListFromWindow (String window){
        final List<String> outputList = new ArrayList<>();
        try {
            final Query query = new QueryParser(MESSAGE_TEXT, analyzer).parse("\"" + window + "\"");
            final IndexReader reader = DirectoryReader.open(index);
            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopDocs docs = searcher.search(query, LUCENE_HITS_PER_PAGE);
            final ScoreDoc[] hits = docs.scoreDocs;
            for (ScoreDoc hit : hits) {
                int docId = hit.doc;
                final Document document = searcher.doc(docId);
                outputList.add(document.get(ID));
            }
        } catch(Exception e){
            log.error(LUCENE_FIND_SMS_ERROR, "Error in finding document using message text. Window - {}. Exception: {}", window, e.getMessage(), e);
        }
        return outputList;
    }

    @TimeIt
    private List<String> findMessage(String window, String textToFind, String[] textToFindArr, int iteration, int start) {
        try {
            List<String> matchFoundList = getMessagesListFromWindow(window);
            if(!matchFoundList.isEmpty() && matchFoundList.size() <= LUCENE_OUTPUT_WINDOW){
                //template match found
                return matchFoundList;
            }
            if (matchFoundList.size() > LUCENE_OUTPUT_WINDOW){
                //increase window size
                iteration++;
                if(start == 0){
                    window = StringUtils.substringBefore(textToFind, textToFindArr[LUCENE_WINDOW_SIZE + iteration]);
                } else {
                    window = StringUtils.substringBetween(textToFind, textToFindArr[start-1] ,textToFindArr[LUCENE_WINDOW_SIZE + iteration]);
                }
                return findMessage(window, textToFind, textToFindArr, iteration, start);
            } else {
                //either variable encountered in the text or template not found in DB
                iteration++;
                if(LUCENE_WINDOW_SIZE + iteration >= textToFindArr.length){
                    return matchFoundList;
                }
                window = StringUtils.substringBetween(textToFind, textToFindArr[start], textToFindArr[LUCENE_WINDOW_SIZE + iteration]);
                start++;
                return findMessage(window, textToFind, textToFindArr, iteration, start);
            }
        } catch (Exception e){
            log.error(LUCENE_FIND_SMS_ERROR, "Error occurred in algorithm of lucene. Window - {}. Exception: {}", window, e.getMessage(), e);
        }
        return null;
    }

    @Override
    @TimeIt
    public MessageTemplateDTO findMessagesFromSmsText(String textToFind) {

        long startTime = System.currentTimeMillis();
        String window = "";
        String[] textToFindArr = textToFind.split(" ");
        if(textToFindArr.length < LUCENE_WINDOW_SIZE){
            window = textToFind;
        } else {
            window = StringUtils.substringBefore(textToFind, textToFindArr[LUCENE_WINDOW_SIZE]);
        }
        List<String> messageIdList = findMessage(window, textToFind, textToFindArr, 0, 0);
        log.info(TIME_TAKEN_TO_FIND_SMS, "Time taken to find - "+ textToFind +" :- " + (System.currentTimeMillis() - startTime));

        if (Objects.isNull(messageIdList)) return null;
        final String convertedMessageText = replaceUniCodesInMessageText(textToFind);
        List<Messages> messagesList = messageIdList.stream()
                .map(id-> messageCachingService.get(id))
                .collect(Collectors.toList());
        return messagesList
                .stream()
                .map(message -> checkIfTemplateMatchesSmsText(message, convertedMessageText))
                .filter(messageTemplateDTO -> Objects.nonNull(messageTemplateDTO))
                .findFirst()
                .orElse(null);
    }

    private String replaceUniCodesInMessageText(String text) {
        return StringEscapeUtils.unescapeJava(text);
    }

    @TimeIt
    private MessageTemplateDTO checkIfTemplateMatchesSmsText(Messages message, String messageText) {
        MessageTemplateDTO messageTemplateDTO = null;
        if (message.isVariablesPresent()) {
            Map<Integer, String> variablesMap = getVarMapIfTemplateMatchesSmsText(message.getMessage(), messageText);
            if (MapUtils.isNotEmpty(variablesMap)) {
                messageTemplateDTO = MessageTemplateDTO.builder().messageTemplateId(message.getTemplateId()).linkedHeader(message.getLinkedHeader()).vars(new ArrayList<>(variablesMap.values())).messageType(message.getMessageType()).sender(message.getSender()).build();
            }
        } else {
            messageTemplateDTO = fetchTemplateByStringComparison(message, messageText);
        }
        return messageTemplateDTO;
    }

    private MessageTemplateDTO fetchTemplateByStringComparison(Messages message, String messageText) {
        return message.getMessage().equals(messageText) ? MessageTemplateDTO.builder().linkedHeader(message.getLinkedHeader()).messageTemplateId(message.getTemplateId()).messageType(message.getMessageType()).sender(message.getSender()).build() : null;
    }

    @TimeIt
    private Map<Integer, String> getVarMapIfTemplateMatchesSmsText(String template, String filledTemplate) {
        Map<Integer, String> templateTranslation = new LinkedHashMap<>();
        String regexTemplate;
        if(template.contains(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix())
                && template.contains(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix())){
            String[] arr = StringUtils.substringsBetween(template, SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix(), SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix());
            for(String str : arr){
                template = template.replace(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix() + str + SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix(), StringEscapeUtils.unescapeJava(PLACE_HOLDER_PATTERN));
            }
            regexTemplate = template.replaceAll("\\+",REPLACE_PATTERN).replaceAll(PLACE_HOLDER_PATTERN, REPLACE_PATTERN);
        } else {
            regexTemplate = template.replaceAll("\\)","").replaceAll("\\(","").replaceAll(PLACE_HOLDER_PATTERN, REPLACE_PATTERN);
        }
        getVariablesMap(template, filledTemplate, templateTranslation, regexTemplate);
        return templateTranslation;
    }

    @TimeIt
    private void getVariablesMap (String template, String filledTemplate, Map<Integer, String> templateTranslation, String regexTemplate) {
        /*Pattern pattern = Pattern.compile(regexTemplate);
        Matcher templateMatcher = pattern.matcher(template);
        Matcher filledTemplateMatcher = pattern.matcher(filledTemplate);
        while (templateMatcher.find() && filledTemplateMatcher.find()) {
            if (templateMatcher.groupCount() == filledTemplateMatcher.groupCount()) {
                for (int i = 1; i <= templateMatcher.groupCount(); i++) {
                    templateTranslation.put(i, filledTemplateMatcher.group(i));
                }
            }
        }*/
        DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Diff> diff = diffMatchPatch.getDiffLinkedList(template, filledTemplate);
        List<DiffMatchPatch.Diff> insertList = diff.stream().filter(diff1 -> diff1.getOperation().equals(DiffMatchPatch.Operation.INSERT)).collect(Collectors.toList());
        List<DiffMatchPatch.Diff> deleteList = diff.stream().filter(diff1 -> diff1.getOperation().equals(DiffMatchPatch.Operation.DELETE)).collect(Collectors.toList());
        if(Objects.equals(insertList.size(), deleteList.size())){
            Optional<DiffMatchPatch.Diff> diffOptional = deleteList.stream().filter(diff1 -> !diff1.getText().contains("{#var#}")).findAny();
            if(!diffOptional.isPresent()){
                int count = 1;
                for(DiffMatchPatch.Diff d : insertList){
                    templateTranslation.put(count++, d.getText());
                }
            }
        }
    }
}
