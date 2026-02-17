package in.wynk.sms.dto.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;

@Getter
@AnalysedEntity
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageStatusCallbackRequest {
    @Analysed
    private String sourceAddress;
    @Analysed
    private Object messageParameters;
    @Analysed
    private String messageRequestId;
    @Analysed
    private String msgStream;
    @Analysed
    private String msgSort;
    @Analysed
    private String messageId;
    @Analysed
    private String sessionId;
    @Analysed
    private long updatedDate;
    @Analysed
    private String msgStatus;
    @Analysed
    private long createdDate;
    @Analysed
    private String messageType;
    @Analysed
    private String customerId;
    @Analysed
    private long sessionLogTime;
    @Analysed
    private String recipientAddress;
}