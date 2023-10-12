package in.wynk.sms.dto.request.whatsapp.session;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.common.dto.wa.outbound.constant.MessageType;
import in.wynk.sms.dto.request.whatsapp.IWhatsappRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Getter
@ToString
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "messageType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextSessionRequest.class, name = "TEXT"),
        @JsonSubTypes.Type(value = MediaSessionRequest.class, name = "MEDIA"),
        @JsonSubTypes.Type(value = ButtonSessionRequest.class, name = "BUTTON"),
        @JsonSubTypes.Type(value = ListSessionRequest.class, name = "LIST"),
        @JsonSubTypes.Type(value = LocationSessionRequest.class, name = "LOCATION"),
        @JsonSubTypes.Type(value = SingleProductSessionRequest.class, name = "SINGLE_PRODUCT"),
        @JsonSubTypes.Type(value = MultiProductSessionRequest.class, name = "MULTI_PRODUCT"),
        @JsonSubTypes.Type(value = ContactsSessionRequest.class, name = "CONTACTS"),
        @JsonSubTypes.Type(value = OrderDetailsSessionRequest.class, name = "ORDER_DETAILS"),
        @JsonSubTypes.Type(value = OrderStatusSessionRequest.class, name = "ORDER_STATUS")
})
public abstract class AbstractSessionRequest implements IWhatsappRequest {
    @NotNull
    @Analysed
    private MessageType messageType;
}
