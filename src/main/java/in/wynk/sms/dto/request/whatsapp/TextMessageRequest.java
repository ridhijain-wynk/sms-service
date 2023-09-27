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
public class TextMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private Message message;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;

        @Override
        public String toString() {
            return "Message{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", message=" + message +
                '}';
    }
}
