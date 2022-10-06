package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.enums.CommunicationType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@ToString
@SuperBuilder
@Document(collection = "senders")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AnalysedEntity
public class Senders extends MongoBaseEntity<String> {
    @Field("bean_name")
    private String name;
    private String description;
    @Field("short_code")
    private String shortCode;
    @Field("entity_id")
    private String entityId;
    private String url;
    private String username;
    private String password;
    @Field("account_name")
    private String accountName;
    private Integer coding;
    private String mClass;
    @Field("client_alias")
    private String clientAlias;
    private SMSPriority priority;

    //voice sender properties
    private VoiceProperties voice;
    //message type specific properties
    @Field("message_type")
    private Map<CommunicationType, MessageTypeSpecificDetails> messageTypeDetails;

    public boolean isUrlPresent(){
        if(url == null){
            return messageTypeDetails != null;
        }
        return true;
    }

    public String getUrl(CommunicationType communicationType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(communicationType)
                && Objects.nonNull(messageTypeDetails.get(communicationType).getUrl())){
            return messageTypeDetails.get(communicationType).getUrl();
        }
        return url;
    }

    public String getUsername(CommunicationType communicationType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(communicationType)
                && Objects.nonNull(messageTypeDetails.get(communicationType).getUsername())){
            return messageTypeDetails.get(communicationType).getUsername();
        }
        return username;
    }

    public String getPassword(CommunicationType communicationType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(communicationType)
                && Objects.nonNull(messageTypeDetails.get(communicationType).getPassword())){
            return messageTypeDetails.get(communicationType).getPassword();
        }
        return password;
    }

}