package in.wynk.sms.queue.message;

import in.wynk.common.dto.IObjectMapper;
import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.constant.SMSSource;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@WynkQueue(queueName = "${sms.priority.high.queue.name}", delaySeconds = "${sms.priority.high.queue.delayInSecond}")
public class HighPriorityMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    private final SMSPriority priority = SMSPriority.HIGH;

    public static HighPriorityMessage from(SmsNotificationMessage smsNotificationMessage) {
        return HighPriorityMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountry().getCountryCode())
                .shortCode(smsNotificationMessage.getShortCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .build();
    }

}
