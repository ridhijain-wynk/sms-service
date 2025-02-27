package in.wynk.sms.dto.request.whatsapp.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleProductSessionRequest extends AbstractSessionRequest implements Serializable {
    private String sessionId;
    private String to;
    private String from;
    private Message message;
    private Product product;

    @Getter
    @Builder
    @AnalysedEntity
    public static class Message {
        private String text;
    }

    @Getter
    @Builder
    @AnalysedEntity
    public static class Product {
        private String catalogId;
        private String productId;
    }
}
