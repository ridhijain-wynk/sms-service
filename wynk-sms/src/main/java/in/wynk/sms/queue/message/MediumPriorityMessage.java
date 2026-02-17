package in.wynk.sms.queue.message;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.common.dto.IObjectMapper;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.stream.advice.WynkKafkaMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@WynkKafkaMessage(topic = "${wynk.kafka.consumers.listenerFactory.medium[0].factoryDetails.topic}", maxRetryCount = 0)
@AnalysedEntity
public class MediumPriorityMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    @Analysed
    private final SMSPriority priority = SMSPriority.MEDIUM;

    public static LowPriorityMessage from(SmsNotificationMessage smsNotificationMessage) {
        return LowPriorityMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.fetchCountry().getCountryCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .templateId(smsNotificationMessage.getMessageId())
                .build();
    }

}
