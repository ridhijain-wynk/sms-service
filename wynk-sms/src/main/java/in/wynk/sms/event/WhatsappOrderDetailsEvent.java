package in.wynk.sms.event;

import com.github.annotation.analytic.core.annotations.Analysed;
import in.wynk.sms.common.dto.wa.outbound.session.OrderDetailsSessionMessage;
import in.wynk.sms.dto.response.WhatsappMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappOrderDetailsEvent {
    @Analysed
    private String requestId;
    @Analysed
    private String sessionId;
    @Analysed
    private String serviceId;
    @Analysed
    private String orgId;
    @Analysed(name = "orderDetailsResponse")
    private WhatsappMessageResponse response;
    @Analysed(name = "orderDetailsMessage")
    private OrderDetailsSessionMessage message;
}