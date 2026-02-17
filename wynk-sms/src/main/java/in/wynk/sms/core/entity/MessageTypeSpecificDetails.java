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
    @Field("app_id")
    private String pinpointAppId;
    @Field("keyword")
    private String pinpointKeyword;
    @Field("sender_id")
    private String pinpointSenderID;
    @Field("origination_number")
    private String pinpointOriginationNumber;
    private boolean enabled;
}