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
public class LocationSessionRequest extends AbstractSessionRequest implements Serializable {
    private String sessionId;
    private String to;
    private String from;
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
