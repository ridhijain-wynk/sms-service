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
public class ButtonMessageRequest extends WhatsappMessageRequest {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private List<Button> buttons;

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
    @Getter
    @Builder
    @AnalysedEntity
    public static class Button {
        private String tag;
        private String title;
    }
}
