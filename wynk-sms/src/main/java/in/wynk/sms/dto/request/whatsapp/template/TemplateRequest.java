package in.wynk.sms.dto.request.whatsapp.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateRequest extends AbstractTemplateRequest implements Serializable {
    private String templateId;
    private String to;
    private String from;
    private List<String> recipients;
    private Message message;
    private MediaAttachment mediaAttachment;
    private CallBackUrls callBackUrls;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private List<String> headerVars;
        private List<String> variables;
        private List<String> payload;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class MediaAttachment {
        private String id;
        private String type;
        private Location location;

        @Getter
        @Builder
        @AnalysedEntity
        public static class Location {
            private String latitude;
            private String longitude;
            private String name;
            private String address;
        }
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class CallBackUrls {
        private String notifyURL;
        private String method;
        private Headers headers;

        @Getter
        @Builder
        @AnalysedEntity
        public static class Headers {
            private String contentType;
        }
    }
}
