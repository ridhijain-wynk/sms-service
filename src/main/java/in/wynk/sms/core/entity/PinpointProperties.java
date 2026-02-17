package in.wynk.sms.core.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PinpointProperties {
    private String appId;
    private String keyword;
    private String senderID;
    @Field("origination_number")
    private String originationNumber;
    private boolean enabled;
}