package in.wynk.sms.core.service;

import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.dto.MessageTemplateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static in.wynk.sms.constants.SMSConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService implements IMessageService {

    private final MessageCachingService messageCachingService;

    @Override
    public MessageTemplateDTO findSenderConfiguredMessageFromSmsText(String messageText) {
        final String convertedMessageText = replaceUniCodesInMessageText(messageText);
        return messageCachingService.getAllSenderConfiguredMessages()
                .stream()
                .map(message -> checkIfTemplateMatchesSmsText(message, convertedMessageText))
                .filter(messageTemplateDTO -> Objects.nonNull(messageTemplateDTO))
                .findFirst()
                .orElse(null);
    }

    @Override
    public MessageTemplateDTO findMessagesFromSmsText(String messageText) {
        final String convertedMessageText = replaceUniCodesInMessageText(messageText);
        return messageCachingService.getAll()
                .stream()
                .map(message -> checkIfTemplateMatchesSmsText(message, convertedMessageText))
                .filter(messageTemplateDTO -> Objects.nonNull(messageTemplateDTO))
                .findFirst()
                .orElse(null);
    }

    private String replaceUniCodesInMessageText(String text) {
        return StringEscapeUtils.unescapeJava(text);
    }

    private MessageTemplateDTO checkIfTemplateMatchesSmsText(Messages message, String messageText) {
        MessageTemplateDTO messageTemplateDTO = null;
        if (message.isVariablesPresent()) {
            Map<Integer, String> variablesMap = getVarMapIfTemplateMatchesSmsText(message.getMessage(), messageText);
            if (MapUtils.isNotEmpty(variablesMap)) {
                messageTemplateDTO = MessageTemplateDTO.builder().messageTemplateId(message.getTemplateId()).linkedHeader(message.getLinkedHeader()).vars(new ArrayList<>(variablesMap.values())).messageType(message.getCommunicationType()).sender(message.getSender()).build();
            }
        } else {
            messageTemplateDTO = fetchTemplateByStringComparison(message, messageText);
        }
        return messageTemplateDTO;
    }

    private MessageTemplateDTO fetchTemplateByStringComparison(Messages message, String messageText) {
        return message.getMessage().equals(messageText) ? MessageTemplateDTO.builder().linkedHeader(message.getLinkedHeader()).messageTemplateId(message.getTemplateId()).messageType(message.getCommunicationType()).sender(message.getSender()).build() : null;
    }

    private Map<Integer, String> getVarMapIfTemplateMatchesSmsText(String template, String filledTemplate) {
        Map<Integer, String> templateTranslation = new LinkedHashMap<>();
        String regexTemplate;
        if(template.contains(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix())
                && template.contains(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix())){
            Pattern pattern = Pattern.compile(Pattern.quote(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix())
                    + SPRING_EXP_REPLACE_PATTERN + Pattern.quote(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix()));
            Matcher matcher = pattern.matcher(template);
            while (matcher.find()) {
                template = template.replace(SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionPrefix() + matcher.group(1) + SMS_MESSAGE_TEMPLATE_CONTEXT.getExpressionSuffix(), StringEscapeUtils.unescapeJava(PLACE_HOLDER_PATTERN));
            }
            regexTemplate = template.replaceAll("\\+",REPLACE_PATTERN).replaceAll(PLACE_HOLDER_PATTERN, REPLACE_PATTERN);
        } else {
            regexTemplate = template.replaceAll("\\)","").replaceAll("\\(","").replaceAll(PLACE_HOLDER_PATTERN, REPLACE_PATTERN);
        }
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
}