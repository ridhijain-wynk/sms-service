package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.MessageTemplate;
import in.wynk.sms.core.repository.MessageTemplateDao;
import in.wynk.sms.dto.MessageTemplateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
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
import static in.wynk.common.constant.CacheBeanNameConstants.MESSAGE_TEMPLATE;
import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;
import static in.wynk.sms.constants.SMSConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service(value = MESSAGE_TEMPLATE)
public class MessageTemplateService implements IMessageTemplateService, IEntityCacheService<MessageTemplate, String> {

    private final Map<String, MessageTemplate> messageTemplateMap = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final MessageTemplateDao messageTemplateDao;
    private final Lock writeLock = lock.writeLock();

    @PostConstruct
    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
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
                messageTemplateMap.clear();
                messageTemplateMap.putAll(localTemplateMap);
            }
        } catch (Throwable th) {
            log.error(APPLICATION_ERROR, "Exception occurred while refreshing message templates cache. Exception: {}", th.getMessage(), th);
            throw th;
        } finally {
            writeLock.unlock();
        }
    }

    private List<MessageTemplate> getActiveMessageTemplateList() {
        return messageTemplateDao.getMessageTemplateByState(State.ACTIVE);
    }

    @Override
    public MessageTemplateDTO findMessageTemplateFromSmsText(String messageText) {
        final String convertedMessageText = replaceUnicodesInMessageText(messageText);
        return messageTemplateMap.values()
                .parallelStream()
                .map(messageTemplate -> checkIfTemplateMatchesSmsText(messageTemplate, convertedMessageText))
                .filter(messageTemplateDTO -> Objects.nonNull(messageTemplateDTO))
                .findFirst()
                .orElse(null);
    }

    private String replaceUnicodesInMessageText(String text) {
        return StringEscapeUtils.unescapeJava(text);
    }

    private MessageTemplateDTO checkIfTemplateMatchesSmsText(MessageTemplate messageTemplate, String messageText) {
        MessageTemplateDTO messageTemplateDTO = null;
        if (messageTemplate.isVariablesPresent()) {
            Map<Integer, String> variablesMap = getVarMapIfTemplateMatchesSmsText(messageTemplate.getTemplateContent(), messageText);
            if (MapUtils.isNotEmpty(variablesMap)) {
                messageTemplateDTO = MessageTemplateDTO.builder().messageTemplateId(messageTemplate.getId()).linkedHeader(messageTemplate.getLinkedHeader()).vars(new ArrayList<>(variablesMap.values())).build();
            }
        } else {
            messageTemplateDTO = fetchTemplateByStringComparison(messageTemplate, messageText);
        }
        return messageTemplateDTO;
    }

    private MessageTemplateDTO fetchTemplateByStringComparison(MessageTemplate messageTemplate, String messageText) {
        return messageTemplate.getTemplateContent().equals(messageText) ? MessageTemplateDTO.builder().linkedHeader(messageTemplate.getLinkedHeader()).messageTemplateId(messageTemplate.getId()).build() : null;
    }

    private Map<Integer, String> getVarMapIfTemplateMatchesSmsText(String template, String filledTemplate) {
        Map<Integer, String> templateTranslation = new LinkedHashMap<>();
        String regexTemplate = template.replaceAll(PLACE_HOLDER_PATTERN, REPLACE_PATTERN);
        Pattern pattern = Pattern.compile(regexTemplate);
        Matcher templateMatcher = pattern.matcher(template);
        Matcher filledTemplateMatcher = pattern.matcher(filledTemplate);
        while (templateMatcher.find() && filledTemplateMatcher.find()) {
            if (templateMatcher.groupCount() == filledTemplateMatcher.groupCount()) {
                for (int i = 1; i <= templateMatcher.groupCount(); i++) {
                    templateTranslation.put(i, filledTemplateMatcher.group(i));
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
        return this.containsKey(key) ? messageTemplateMap.get(key) : MessageTemplate.builder().id(UNKNOWN).build();
    }

    @Override
    public Collection<MessageTemplate> getAll() {
        return messageTemplateMap.values();
    }

    @Override
    public boolean containsKey(String key) {
        return messageTemplateMap.containsKey(key);
    }

}