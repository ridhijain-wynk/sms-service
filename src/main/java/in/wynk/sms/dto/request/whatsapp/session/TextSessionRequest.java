package in.wynk.sms.dto.request.whatsapp.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.dto.request.whatsapp.WhatsappMessageRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("TEXT")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextSessionRequest extends WhatsappMessageRequest implements Serializable {
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
