package in.wynk.sms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;

@Getter
@Builder
@AnalysedEntity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappMessageResponse {
    private int code;
    private boolean success;
    private String from;
    private String messageRequestId;
    private String status;
}