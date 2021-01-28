package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public abstract class SmsRequest {

    @Analysed
    @JsonProperty("message")
    private String text;
    @Analysed
    @Setter //temporary
    private String msisdn;
    @Analysed
    private String countryCode;
    @Analysed
    @ApiModelProperty(hidden = true)
    @Setter
    private String service;
    @Setter
    @Analysed
    @ApiModelProperty(hidden = true)
    private String clientAlias;
    @Analysed
    @ApiModelProperty(hidden = true)
    @Setter
    private String shortCode;
    @Analysed
    private String messageId;

    public abstract SMSPriority getPriority();
}
