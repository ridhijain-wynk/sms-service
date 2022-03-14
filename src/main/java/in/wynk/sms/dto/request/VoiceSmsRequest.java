package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "priority")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HighPriorityMessage.class, name = "HIGH"),
        @JsonSubTypes.Type(value = MediumPriorityMessage.class, name = "MEDIUM"),
        @JsonSubTypes.Type(value = LowPriorityMessage.class, name = "LOW")
})
@Getter
@AnalysedEntity
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class VoiceSmsRequest {

    @Analysed
    private String callFlowId;
    @Analysed
    private String customerId;
    @Analysed
    private String callType;
    @Analysed
    private CallFlowConfiguration callFlowConfiguration;

}
