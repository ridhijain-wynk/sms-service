package in.wynk.sms.enums;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AnalysedEntity
@RequiredArgsConstructor
public enum WhatsappMessageType {
    TEXT("TEXT"), MEDIA("MEDIA"), BUTTON("BUTTON"), LIST("LIST"), LOCATION("LOCATION"), SINGLE_PRODUCT("SINGLE_PRODUCT"), MULTI_PRODUCT("MULTI_PRODUCT"), CONTACTS("CONTACTS"), ORDER_DETAILS("ORDER_DETAILS"), ORDER_STATUS("ORDER_STATUS");

    @Analysed
    private final String type;
}