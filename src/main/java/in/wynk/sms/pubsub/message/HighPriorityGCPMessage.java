package in.wynk.sms.pubsub.message;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.common.dto.IObjectMapper;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationGCPMessage;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
//@WynkPubSub(projectName = "${sms.priority.high.pubSub.projectName}", topicName = "${sms.priority.high.pubSub.topicName}", subscriptionName = "${sms.priority.high.pubSub.subscriptionName}", bufferInterval = "${sms.priority.high.pubSub.bufferInterval}")
@AnalysedEntity
public class HighPriorityGCPMessage extends SmsRequest implements IObjectMapper {


    @Builder.Default
    @Analysed
    private final SMSPriority priority = SMSPriority.HIGH;

    @Override
    public SMSPriority getPriority() {
        return SMSPriority.HIGH;
    }

    public static HighPriorityGCPMessage from(SmsNotificationGCPMessage smsNotificationMessage) {
        return HighPriorityGCPMessage.builder()
                .messageId(smsNotificationMessage.getMsisdn() + System.currentTimeMillis())
                .countryCode(smsNotificationMessage.getCountry().getCountryCode())
                .service(smsNotificationMessage.getService())
                .msisdn(smsNotificationMessage.getMsisdn())
                .text(smsNotificationMessage.getMessage())
                .templateId(smsNotificationMessage.getMessageId())
                .build();
    }
}
