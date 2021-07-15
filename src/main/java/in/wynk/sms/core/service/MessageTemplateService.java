package in.wynk.sms.core.service;

import in.wynk.data.dto.IEntityCacheService;
import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.MessageTemplate;
import in.wynk.sms.core.repository.MessageTemplateDao;
import in.wynk.sms.dto.MessageTemplateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.wynk.common.constant.BaseConstants.IN_MEMORY_CACHE_CRON;
import static in.wynk.common.constant.BaseConstants.UNKNOWN;
import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;
import static in.wynk.sms.constants.SMSConstants.PLACE_HOLDER_PATTERN;
import static in.wynk.sms.constants.SMSConstants.REPLACE_PATTERN;

@Service
@Slf4j
public class MessageTemplateService implements IMessageTemplateService, IEntityCacheService<MessageTemplate, String> {

    private final Map<String, MessageTemplate> messageTemplateMap = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();

    @Autowired
    MessageTemplateDao messageTemplateDao;

    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
    @PostConstruct
    public void init() {
        loadMessageTemplates();
    }

    public void loadMessageTemplates() {
        try {
            List<MessageTemplate> messageTemplateList = getActiveMessageTemplateList();
            if (writeLock.tryLock() && !CollectionUtils.isEmpty(messageTemplateList)) {
                Map<String, MessageTemplate> localTemplateMap = new ConcurrentHashMap<>();
                for (MessageTemplate messageTemplate : messageTemplateList) {
                    localTemplateMap.put(messageTemplate.getId(), messageTemplate);
                }
                messageTemplateMap.putAll(localTemplateMap);
            }
        } catch (Throwable th) {
            log.error(APPLICATION_ERROR, "Exception occurred while refreshing message templates cache. Exception: {}", th.getMessage(), th);
            throw th;
        } finally {
            writeLock.unlock();
        }
    }

    private List<MessageTemplate> getActiveMessageTemplateList(){
        return messageTemplateDao.getMessageTemplateByState(State.ACTIVE);
    }

    @Override
    public MessageTemplateDTO findMessageTemplateFromSmsText(String message) {
        Optional<MessageTemplateDTO> result = messageTemplateMap.values().parallelStream().map(messageTemplate -> checkIfTemplateMatchesSmsText(messageTemplate,message)).filter(messageTemplateDTO -> Objects.nonNull(messageTemplateDTO)).findFirst();
        return result.isPresent()?result.get():null;
    }

    private MessageTemplateDTO checkIfTemplateMatchesSmsText(MessageTemplate messageTemplate, String message) {
        MessageTemplateDTO messageTemplateDTO = null;
        if(messageTemplate.isVariablesPresent()) {
            Map<Integer, String> variablesMap = getVarMapIfTemplateMatchesSmsText(messageTemplate.getTemplateContent(), message);
            if (MapUtils.isNotEmpty(variablesMap)) {
                messageTemplateDTO = MessageTemplateDTO.builder().messageTemplateId(messageTemplate.getId()).linkedHeader(messageTemplate.getLinkedHeader()).vars(new ArrayList<>(variablesMap.values())).build();
            }
        } else {
            messageTemplateDTO = fetchTemplateByStringComparison(messageTemplate,message);
        }
        return messageTemplateDTO;
    }

    private MessageTemplateDTO fetchTemplateByStringComparison(MessageTemplate messageTemplate,String message) {
        if(messageTemplate.getTemplateContent().equals(message)) {
            return MessageTemplateDTO.builder().linkedHeader(messageTemplate.getLinkedHeader())
                    .messageTemplateId(messageTemplate.getId())
                    .build();
        }
        return null;
    }

    private Map<Integer, String> getVarMapIfTemplateMatchesSmsText(String template, String filledTemplate){
        Map<Integer, String> templateTranslation = new LinkedHashMap<>();
        String regexTemplate = template.replaceAll(PLACE_HOLDER_PATTERN,REPLACE_PATTERN);
        Pattern pattern = Pattern.compile(regexTemplate);
        Matcher templateMatcher = pattern.matcher(template);
        Matcher filledTemplateMatcher = pattern.matcher(filledTemplate);

        while (templateMatcher.find() && filledTemplateMatcher.find()) {
            if(templateMatcher.groupCount() == filledTemplateMatcher.groupCount()){
                for (int i = 1; i <= templateMatcher.groupCount(); i++) {
                    templateTranslation.put(
                            i,
                            filledTemplateMatcher.group(i)
                    );
                }
            }
        }

        return templateTranslation;
    }

    @Override
    public Collection<MessageTemplate> getAllByState(State state) {
        return messageTemplateDao.getMessageTemplateByState(state);
    }

    @Override
    public MessageTemplate save(MessageTemplate item) {
        return messageTemplateDao.save(item);
    }

    @Override
    public MessageTemplate get(String key) {
        if(messageTemplateMap.containsKey(key)) {
            return messageTemplateMap.get(key);
        }
        return MessageTemplate.builder().id(UNKNOWN).build();
    }

    @Override
    public Collection<MessageTemplate> getAll() {
        return messageTemplateMap.values();
    }
}
