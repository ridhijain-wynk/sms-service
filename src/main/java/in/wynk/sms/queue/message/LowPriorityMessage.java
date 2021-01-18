package in.wynk.sms.queue.message;

import in.wynk.common.dto.IObjectMapper;
import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.constant.SMSSource;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@WynkQueue(queueName = "${sms.priority.low.queue.name}", delaySeconds = "${sms.priority.low.queue.delayInSecond}")
public class LowPriorityMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    private final SMSPriority priority = SMSPriority.LOW;

    public static LowPriorityMessage from(SmsNotificationMessage smsNotificationMessage) {
        return LowPriorityMessage.builder()
                .shortCode(SMSSource.getShortCode(smsNotificationMessage.getSource(), SMSPriority.HIGH))
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountryCode())
                .service(smsNotificationMessage.getSource())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .build();
    }

}
