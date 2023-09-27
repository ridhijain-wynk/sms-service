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
public class MediaMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private MediaAttachment mediaAttachment;

    @Getter
    @Builder
    @AnalysedEntity
    public static class MediaAttachment {
        private String type;
        private String url;
        private String caption;

        @Override
        public String toString() {
            return "MediaAttachment{" +
                    "type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", caption='" + caption + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "sessionId='" + sessionId + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", mediaAttachment=" + mediaAttachment +
                '}';
    }
}
