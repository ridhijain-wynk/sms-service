package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.enums.CommunicationType;
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
public class IQSmsRequest {

    String customerId;
    List<String> destinationAddress;
    String sourceAddress;
    String message;
    String messageType;
    String dltTemplateId;
    String entityId;

    public static IQSmsRequest from(MessageTemplateDTO messageTemplateDTO, SmsRequest smsRequest,String customerId,String entityId) {
        IQSmsRequestBuilder builder = IQSmsRequest.builder();
        if(Objects.nonNull(messageTemplateDTO) && Objects.nonNull(smsRequest)) {
            builder.customerId(customerId)
                    .destinationAddress(Arrays.asList(smsRequest.getMsisdn()))
                    .sourceAddress(messageTemplateDTO.getLinkedHeader())
                    .message(smsRequest.getText())
                    .messageType(messageTemplateDTO.getMessageType().getType())
                    .dltTemplateId(messageTemplateDTO.getMessageTemplateId())
                    .entityId(entityId);
        }
        return builder.build();
    }
}
