package in.wynk.sms.dto.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.common.dto.wa.outbound.session.*;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateMultiRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateSingleRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.SingleTemplateMultiRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.SingleTemplateSingleRecipientMessage;
import in.wynk.sms.dto.request.whatsapp.session.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@ToString
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType",
        visible = true
)
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
        @JsonSubTypes.Type(value = OrderStatusSessionRequest.class, name = "ORDER_STATUS"),

        @JsonSubTypes.Type(value = ListSessionMessage.class, name = "LIST"),
        @JsonSubTypes.Type(value = TextSessionMessage.class, name = "TEXT"),
        @JsonSubTypes.Type(value = MediaSessionMessage.class, name = "MEDIA"),
        @JsonSubTypes.Type(value = ButtonSessionMessage.class, name = "BUTTON"),
        @JsonSubTypes.Type(value = LocationSessionMessage.class, name = "LOCATION"),
        @JsonSubTypes.Type(value = ContactsSessionMessage.class, name = "CONTACTS"),
        @JsonSubTypes.Type(value = OrderStatusSessionMessage.class, name = "ORDER_STATUS"),
        @JsonSubTypes.Type(value = OrderDetailsSessionMessage.class, name = "ORDER_DETAILS"),
        @JsonSubTypes.Type(value = MultiProductSessionMessage.class, name = "MULTI_PRODUCT"),
        @JsonSubTypes.Type(value = SingleProductSessionMessage.class, name = "SINGLE_PRODUCT"),
        @JsonSubTypes.Type(value = SingleTemplateMultiRecipientMessage.class, name = "MULTI_TEMPLATE"),
        @JsonSubTypes.Type(value = SingleTemplateSingleRecipientMessage.class, name = "SINGLE_TEMPLATE"),
        @JsonSubTypes.Type(value = BulkTemplateMultiRecipientMessage.class, name = "BULK_MULTI_TEMPLATE"),
        @JsonSubTypes.Type(value = BulkTemplateSingleRecipientMessage.class, name = "BULK_SINGLE_TEMPLATE"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class WhatsappMessageRequest implements IWhatsappRequest, Serializable {
    @Analysed
    private String sessionId;
    @Analysed
    @NotNull
    private String msisdn;
    @Analysed
    private String message;
    @Analysed
    @NotNull
    private String messageType;
    @Analysed
    private String countryCode;
    @Analysed
    private String priority;
    @Analysed
    private int retryCount;
    @Analysed
    @NotNull
    private String service;

    @NotNull
    @Analysed
    private String clientAlias;
    @Analysed
    private IWhatsappRequest whatsappRequest;
}