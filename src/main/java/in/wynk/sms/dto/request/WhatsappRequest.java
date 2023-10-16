package in.wynk.sms.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.common.dto.wa.outbound.common.ListMessage;
import in.wynk.sms.common.dto.wa.outbound.common.MediaAttachment;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsappRequest implements Serializable {
    @Analysed
    @NotNull
    @JsonProperty("msisdn")
    private String to;
    @Analysed
    @JsonProperty("message")
    private String text;
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

    @Analysed
    private String templateId;
    @Analysed
    private String sessionId;
    @Analysed
    private String from;
    @Analysed
    private MediaAttachment mediaAttachment;
    @Analysed
    private ListMessage list;
    @Analysed
    private List<Button> buttons;
    @Analysed
    private List<Contact> contacts;
    @Analysed
    private List<OrderDetail> orderDetails;
    @Analysed
    private OrderStatus orderStatus;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Button {
        private String tag;
        private String title;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class Contact {
        private List<Contact.Address> addresses;
        private String birthday;
        private List<Contact.Email> emails;
        private Contact.Name name;
        private Contact.Org org;
        private List<Contact.Phone> phones;
        private List<Contact.Url> urls;

        @Getter
        @Builder
        @AnalysedEntity
        public static class Address {
            private String city;
            private String country;
            private String country_code;
            private String state;
            private String street;
            private String type;
            private String zip;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Email {
            private String email;
            private String type;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Name {
            private String first_name;
            private String formatted_name;
            private String last_name;
            private String suffix;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Org {
            private String company;
            private String department;
            private String title;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Phone {
            private String phone;
            private String wa_id;
            private String type;
        }

        @Getter
        @Builder
        @AnalysedEntity
        public static class Url {
            private String url;
            private String type;
        }
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class OrderDetail {
        private String type;
        private String payment_configuration;
        private OrderDetail.TotalAmount total_amount;
        private OrderDetail.Order order;
        private Subtotal subtotal;
        private Tax tax;
        private Shipping shipping;
        private Discount discount;

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
            private String catalog_id;
            private Expiration expiration;
            private List<Item> items;
        }

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
            private String retailer_id;
            private String name;
            private Amount amount;
            private int quantity;
            private Amount sale_amount;
            private String country_of_origin;
            private String importer_name;
            private ImporterAddress importer_address;
        }

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
        public static class ImporterAddress {
            private String address_line1;
            private String address_line2;
            private String city;
            private String zone_code;
            private String postal_code;
            private String country_code;
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
        private String discount_program_name;
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