package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.utils.SMSUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@AnalysedEntity
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level= AccessLevel. PRIVATE)
@Builder
@ToString
public class IQSmsRequest {

    String customerId;
    List<String> destinationAddress;
    String sourceAddress;
    String message;
    String messageType;
    String dltTemplateId;
    String entityId;
    String otp;

    public static IQSmsRequest from(MessageTemplateDTO messageTemplateDTO, SmsRequest smsRequest, String clientAlias, Senders senders, String customerId, String entityId, String countryCode) {
        IQSmsRequestBuilder builder = IQSmsRequest.builder();
        if(Objects.nonNull(messageTemplateDTO) && Objects.nonNull(smsRequest)) {
            final String shortCode = SMSUtils.getShortCode(messageTemplateDTO.getMessageTemplateId(), smsRequest.getPriority(), clientAlias, senders.getShortCode(), countryCode);
            builder.customerId(customerId)
                    .destinationAddress(Arrays.asList(smsRequest.getMsisdn()))
                    .sourceAddress(shortCode.contains("-")?shortCode.split("-")[1]:shortCode)
                    .message(smsRequest.getText())
                    .messageType(messageTemplateDTO.getMessageType().getType())
                    .dltTemplateId(messageTemplateDTO.getMessageTemplateId())
                    .entityId(entityId);
        }
        switch (smsRequest.getPriority()) {
            case HIGHEST:
            case HIGH:
                builder.otp("true");
                break;
            default:
                break;
        }
        return builder.build();
    }
}
