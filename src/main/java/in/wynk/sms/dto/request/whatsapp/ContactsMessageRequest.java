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
public class ContactsMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private List<Contact> contacts;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Contact {
        private List<Address> addresses;
        private String birthday;
        private List<Email> emails;
        private Name name;
        private Org org;
        private List<Phone> phones;
        private List<Url> urls;

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
}
