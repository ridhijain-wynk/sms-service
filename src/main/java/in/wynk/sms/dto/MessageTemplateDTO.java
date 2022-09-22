package in.wynk.sms.dto;

import in.wynk.sms.enums.CommunicationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MessageTemplateDTO {
    private List<String> vars;
    private String messageTemplateId;
    private String linkedHeader;
    private CommunicationType messageType;
}
