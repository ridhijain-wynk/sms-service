package in.wynk.sms.queue.message;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.common.dto.IObjectMapper;
import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@WynkQueue(queueName = "${sms.priority.medium.queue.name}", delaySeconds = "${sms.priority.medium.queue.delayInSecond}", maxRetryCount = 0)
@AnalysedEntity
public class MediumPriorityMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    @Analysed
    private final SMSPriority priority = SMSPriority.MEDIUM;

    public static LowPriorityMessage from(SmsNotificationMessage smsNotificationMessage) {
        return LowPriorityMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountry().getCountryCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .build();
    }

}
