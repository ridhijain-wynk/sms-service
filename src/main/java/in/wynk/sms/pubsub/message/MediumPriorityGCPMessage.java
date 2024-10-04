package in.wynk.sms.pubsub.message;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.common.dto.IObjectMapper;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationGCPMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.stream.advice.WynkKafkaMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@AnalysedEntity
@WynkKafkaMessage(topic = "${sms.priority.medium.kafka.topic}")
public class MediumPriorityGCPMessage extends SmsRequest implements IObjectMapper {

    @Builder.Default
    @Analysed
    private final SMSPriority priority = SMSPriority.MEDIUM;

    public static LowPriorityGCPMessage from(SmsNotificationGCPMessage smsNotificationMessage) {
        return LowPriorityGCPMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountry().getCountryCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .templateId(smsNotificationMessage.getMessageId())
                .build();
    }

}
