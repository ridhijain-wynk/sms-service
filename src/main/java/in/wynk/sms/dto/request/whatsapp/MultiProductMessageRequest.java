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
public class MultiProductMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private Catalog catalog;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class Catalog {
        private String heading;
        private String catalogId;
        private List<Section> sections;

        @Getter
        @Builder
        @AnalysedEntity
        public static class Section {
            private String heading;
            private List<String> products;
        }
    }
}