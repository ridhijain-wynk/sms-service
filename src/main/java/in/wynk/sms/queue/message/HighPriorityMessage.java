package in.wynk.sms.queue.message;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.common.dto.IObjectMapper;
import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.stream.advice.DelayedKafkaEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@SuperBuilder
@Getter
@NoArgsConstructor
@DelayedKafkaEvent(topic = "${wynk.kafka.consumers.listenerFactory.high[0].factoryDetails.topic}", maxRetryCount = 0)
@AnalysedEntity
public class HighPriorityMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    @Analysed
    private final SMSPriority priority = SMSPriority.HIGH;

    public static HighPriorityMessage from(SmsNotificationMessage smsNotificationMessage) {
        return HighPriorityMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountry().getCountryCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .templateId(smsNotificationMessage.getMessageId())
                .build();
    }

}
