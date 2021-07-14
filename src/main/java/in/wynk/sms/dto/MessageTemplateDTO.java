package in.wynk.sms.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MessageTemplateDTO {
    private List<String> vars;
    private String messageTemplateId;
    private int linkedHeader;
}
