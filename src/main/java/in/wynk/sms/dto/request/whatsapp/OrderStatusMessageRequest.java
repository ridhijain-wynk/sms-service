package in.wynk.sms.dto.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private OrderStatus orderStatus;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class OrderStatus {
        private String order_id;
        private String reference_id;
        private Order order;

        @Getter
        @Builder
        @AnalysedEntity
        public static class Order {
            private String status;
            private String description;
        }
    }
}