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
        AnalyticService.update(event);
        if (StringUtils.isNotEmpty(event.getMsisdn()) && Objects.nonNull(event.getContextMap())) {
            String circleCode = String.valueOf(event.getContextMap().getOrDefault(CIRCLE_CODE, SMSConstants.DEFAULT));
            Messages message = messageCachingService.get(event.getMessageId().concat(circleCode));
            if (Objects.isNull(message)) {
                message = messageCachingService.get(event.getMessageId());
            }

            //todo: this check can be removed
            if(Objects.isNull(message)){
                log.error(MESSAGE_NOT_FOUND, "Unable to find linked message {} ", event.getMessageId());
                return;
            }

            final String smsMessage = message.getMessage();
            if(message.isEnabled()){
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
