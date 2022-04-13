package in.wynk.sms.listener;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.repository.MessagesDao;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.spel.IRuleEvaluator;
import in.wynk.spel.builder.DefaultStandardExpressionContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import java.util.Objects;

import static in.wynk.sms.constants.SmsLoggingMarkers.NO_MESSAGE_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsEventsListener {

    private final IRuleEvaluator ruleEvaluator;
    private final ISqsManagerService sqsManagerService;
    private final MessageCachingService messageCachingService;

    @EventListener
    @AnalyseTransaction(name = "evaluateNotificationMessage")
    public void evaluateMessage(SmsNotificationMessage event) {
        if (StringUtils.isNotEmpty(event.getMsisdn()) && Objects.nonNull(event.getPlanOfferDataMap())) {
            Messages message = messageCachingService.get(event.getMessageId().concat((Objects.isNull(event.getCircleCode()))?"":event.getCircleCode()));
            if (Objects.isNull(message)) {
                message = messageCachingService.get(event.getMessageId());
            }

            //todo: this check can be removed
            if(Objects.isNull(message)){
                log.error(NO_MESSAGE_FOUND, "Unable to find linked message {} ", event.getMessageId());
                return;
            }

            final String smsMessage = message.getMessage();
            if(message.isEnabled()){
                final StandardEvaluationContext seContext = DefaultStandardExpressionContextBuilder.builder()
                        .variable(SMSConstants.MESSAGE_DATA_MAP, event.getPlanOfferDataMap())
                        .variable(SMSConstants.CIRCLE_CODE, Objects.isNull(event.getCircleCode())?SMSConstants.DEFAULT:event.getCircleCode())
                        .variable(SMSConstants.REMINDER_COUNT, event.getReminderCount())
                        .build();
                final String evaluatedMessage = ruleEvaluator.evaluate(smsMessage, () -> seContext, SMSConstants.SMS_MESSAGE_TEMPLATE_CONTEXT, String.class);
                event.setMessage(evaluatedMessage);
                event.setPriority(message.getPriority());

                AnalyticService.update(event);
                SmsRequest smsRequest = SMSFactory.getSmsRequest(event);
                sqsManagerService.publishSQSMessage(smsRequest);
            }
        }
    }
}
