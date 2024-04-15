package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.pubsub.message.HighPriorityGCPMessage;
import in.wynk.sms.pubsub.message.HighestPriorityGCPMessage;
import in.wynk.sms.pubsub.message.LowPriorityGCPMessage;
import in.wynk.sms.pubsub.message.MediumPriorityGCPMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "priority")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HighestPriorityGCPMessage.class, name = "HIGHEST"),
        @JsonSubTypes.Type(value = HighPriorityGCPMessage.class, name = "HIGH"),
        @JsonSubTypes.Type(value = MediumPriorityGCPMessage.class, name = "MEDIUM"),
        @JsonSubTypes.Type(value = LowPriorityGCPMessage.class, name = "LOW")
})
@Getter
@AnalysedEntity
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SmsRequest implements Serializable {

    private String message;
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
    @Analysed
    private String templateId;
    @Analysed
    private Integer retryCount;

    @Setter
    @Builder.Default
    private CommunicationType communicationType = CommunicationType.SMS;

    public abstract SMSPriority getPriority();

    @Analysed(name = "text")
    public String getText() {
        if (StringUtils.isBlank(text)) {
            return message;
        }
        return text;
    }

    @JsonIgnore
    public boolean isEnglish() {
        final String message = getText();
        for (int i = 0; i < message.length(); ++i) {
            int asciiValue = message.charAt(i);
            if (asciiValue < 32 || asciiValue > 126) {
                return false;
            }
        }
        return true;
    }


}
