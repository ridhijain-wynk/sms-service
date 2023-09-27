package in.wynk.sms.dto.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@ToString
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "messageType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessageRequest.class, name = "TEXT"),
        @JsonSubTypes.Type(value = MediaMessageRequest.class, name = "MEDIA"),
        @JsonSubTypes.Type(value = ButtonMessageRequest.class, name = "BUTTON"),
        @JsonSubTypes.Type(value = ListMessageRequest.class, name = "LIST"),
        @JsonSubTypes.Type(value = LocationMessageRequest.class, name = "LOCATION"),
        @JsonSubTypes.Type(value = SingleProductMessageRequest.class, name = "SINGLE_PRODUCT"),
        @JsonSubTypes.Type(value = MultiProductMessageRequest.class, name = "MULTI_PRODUCT"),
        @JsonSubTypes.Type(value = ContactsMessageRequest.class, name = "CONTACTS"),
        @JsonSubTypes.Type(value = OrderDetailsMessageRequest.class, name = "ORDER_DETAILS"),
        @JsonSubTypes.Type(value = OrderStatusMessageRequest.class, name = "ORDER_STATUS")
})
public abstract class WhatsappMessageRequest implements Serializable {
    @NotNull
    @Analysed
    private String messageType;
    @NotNull
    @Analysed
    private String clientAlias;
    @Analysed
    private WhatsappMessageRequest messageRequest;
    //kafka routing - kafka consumer - client driven
}
