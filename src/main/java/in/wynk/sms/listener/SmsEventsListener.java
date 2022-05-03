package in.wynk.sms.listener;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.event.SmsNotificationEvent;
import in.wynk.spel.IRuleEvaluator;
import in.wynk.spel.builder.DefaultStandardExpressionContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static in.wynk.common.constant.BaseConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.MESSAGE_NOT_FOUND;
import static in.wynk.sms.constants.SmsLoggingMarkers.OLD_MESSAGE_PATTERN;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsEventsListener {

    private final IRuleEvaluator ruleEvaluator;
    private final ISqsManagerService sqsManagerService;
    private final MessageCachingService messageCachingService;

    @EventListener
    @AnalyseTransaction(name = "smsNotificationEvent")
    public void onSmsNotificationEvent(SmsNotificationEvent event) {
        if (StringUtils.isNotEmpty(event.getMsisdn())) {
            if (StringUtils.isNotEmpty(event.getMessage())) {
                log.info(OLD_MESSAGE_PATTERN, "Resolved message present for ", event.getMsisdn());
                SmsRequest smsRequest = SMSFactory.getSmsRequest(SmsNotificationMessage.builder()
                        .message(event.getMessage())
                        .msisdn(event.getMsisdn())
                        .service(event.getService())
                        .priority(event.getPriority())
                        .build());
                sqsManagerService.publishSQSMessage(smsRequest);
            } else if (Objects.nonNull(event.getContextMap())) {
                final String circleCode = String.valueOf(event.getContextMap().get(CIRCLE_CODE));
                Messages message = getMessage(event.getMessageId(), circleCode);

                if (Objects.isNull(message)) {
                    log.error(MESSAGE_NOT_FOUND, "Unable to find linked message {} ", event.getMessageId());
                    return;
                }

                final String smsMessage = message.getMessage();
                if (message.isEnabled()) {
                    final StandardEvaluationContext seContext = DefaultStandardExpressionContextBuilder.builder()
                            .variable(CONTEXT_MAP, event.getContextMap())
                            .build();
                    final String evaluatedMessage = ruleEvaluator.evaluate(smsMessage, () -> seContext, SMS_MESSAGE_TEMPLATE_CONTEXT, String.class);

                    SmsRequest smsRequest = SMSFactory.getSmsRequest(SmsNotificationMessage.builder()
                            .message(evaluatedMessage)
                            .msisdn(event.getMsisdn())
                            .service(event.getService())
                            .priority(message.getPriority())
                            .build());
                    sqsManagerService.publishSQSMessage(smsRequest);
                }
            }
        }
    }

    private Messages getMessage(String messageId, String circleCode) {
        if (StringUtils.equalsIgnoreCase(circleCode, SMSConstants.DEFAULT)) {
            return messageCachingService.get(messageId);
        } else {
            Messages message = messageCachingService.get(messageId.concat(circleCode));
            if (Objects.isNull(message)) {
                return messageCachingService.get(messageId);
            }
            return message;
        }
    }
}
