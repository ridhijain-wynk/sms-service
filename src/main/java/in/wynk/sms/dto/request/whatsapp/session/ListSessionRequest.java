package in.wynk.sms.dto.request.whatsapp.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListSessionRequest extends AbstractSessionRequest implements Serializable {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private ListMessage list;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class ListMessage {
        private String heading;
        private List<Option> options;
    }
    @Getter
    @Builder
    @AnalysedEntity
    public static class Option {
        private String tag;
        private String title;
        private String description;
    }
}
