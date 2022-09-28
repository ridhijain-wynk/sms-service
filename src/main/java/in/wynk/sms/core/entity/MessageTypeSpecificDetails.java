package in.wynk.sms.core.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageTypeSpecificDetails {
    private String url;
    private String username;
    private String password;
    @Field("short_code")
    private String shortCode;
}