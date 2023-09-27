package in.wynk.sms.dto.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailsMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private OrderDetails orderDetails;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class OrderDetails {
        private String type;
        private String paymentConfiguration;
        private TotalAmount totalAmount;
        private Order order;

        @Getter
        @Builder
        @AnalysedEntity
        public static class TotalAmount {
            private int value;
            private int offset;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Order {
            private String catalogId;
            private Expiration expiration;
            private List<Item> items;
            private Subtotal subtotal;
            private Tax tax;
            private Shipping shipping;
            private Discount discount;

            @Getter
            @Builder
            @AnalysedEntity
            public static class Expiration {
                private int timestamp;
                private String description;
            }

            @Getter
            @Builder
            @AnalysedEntity
            public static class Item {
                private String retailerId;
                private String name;
                private Amount amount;
                private int quantity;
                private SaleAmount saleAmount;
                private String countryOfOrigin;
                private String importerName;
                private ImporterAddress importerAddress;

                @Getter
                @Builder
                @AnalysedEntity
                public static class Amount {
                    private int value;
                    private int offset;
                }

                @Getter
                @Builder
                @AnalysedEntity
                public static class SaleAmount {
                    private int value;
                    private int offset;
                }

                @Getter
                @Builder
                @AnalysedEntity
                public static class ImporterAddress {
                    private String addressLine1;
                    private String addressLine2;
                    private String city;
                    private String zoneCode;
                    private String postalCode;
                    private String countryCode;
                }
            }

            @Getter
            @Builder
            @AnalysedEntity
            public static class Subtotal {
                private int value;
                private int offset;
            }

            @Getter
            @Builder
            @AnalysedEntity
            public static class Tax {
                private int value;
                private int offset;
                private String description;
            }

            @Getter
            @Builder
            @AnalysedEntity
            public static class Shipping {
                private int value;
                private int offset;
                private String description;
            }

            @Getter
            @Builder
            @AnalysedEntity
            public static class Discount {
                private int value;
                private int offset;
                private String description;
                private String discountProgramName;
            }
        }
    }
}
