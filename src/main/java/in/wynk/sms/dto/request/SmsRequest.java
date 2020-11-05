package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
public abstract class SmsRequest {

    @Analysed
    private String text;
    @Analysed
    private String msisdn;
    @Analysed
    private String countryCode;
    @Analysed
    @ApiModelProperty(hidden = true)
    @Setter
    private String service;
    @Analysed
    @ApiModelProperty(hidden = true)
    @Setter
    private String shortCode;
    @Analysed
    private String messageId;

    public abstract SMSPriority getPriority();
}
