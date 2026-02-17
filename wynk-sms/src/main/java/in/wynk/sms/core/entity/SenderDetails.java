package in.wynk.sms.core.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SenderDetails {
    private String primary;
    private String secondary;
    @Field("scrubbing_enabled")
    private boolean scrubbingEnabled;
    @Field("short_code")
    private String shortCode;

    public boolean isPrimaryPresent() {
        return primary != null;
    }
    public boolean isSecondaryPresent() {
        return secondary != null;
    }
}