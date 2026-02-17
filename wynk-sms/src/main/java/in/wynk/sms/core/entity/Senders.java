package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import in.wynk.rate.limiter.dto.RateLimit;
import in.wynk.sms.enums.MessageType;
import in.wynk.sms.common.constant.SMSPriority;
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
    @Field("country_code")
    private String countryCode;
    private SMSPriority priority;

    //voice sender properties
    private VoiceProperties voice;
    //message type specific properties
    @Field("message_type")
    private Map<MessageType, MessageTypeSpecificDetails> messageTypeDetails;
    @Field("rate_limit")
    private RateLimit rateLimit;
    @Field("waba_number")
    private String WABANumber;

    public boolean isUrlPresent(){
        if(url == null){
            return messageTypeDetails != null;
        }
        return true;
    }

    public String getUrl(MessageType messageType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(messageType)
                && Objects.nonNull(messageTypeDetails.get(messageType).getUrl())){
            return messageTypeDetails.get(messageType).getUrl();
        }
        return url;
    }

    public String getUsername(MessageType messageType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(messageType)
                && Objects.nonNull(messageTypeDetails.get(messageType).getUsername())){
            return messageTypeDetails.get(messageType).getUsername();
        }
        return username;
    }

    public String getPassword(MessageType messageType){
        if(Objects.nonNull(messageTypeDetails) && messageTypeDetails.containsKey(messageType)
                && Objects.nonNull(messageTypeDetails.get(messageType).getPassword())){
            return messageTypeDetails.get(messageType).getPassword();
        }
        return password;
    }

}