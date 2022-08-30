package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
@SuperBuilder
@Document(collection = "senders")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AnalysedEntity
public class Senders extends MongoBaseEntity<String> {
    private String name;
    private String description;
    @Field("short_code")
    private String shortCode;
    private String url;
    private String username;
    private String password;
    private String accountName;
    private Integer coding;
    private String mClass;
    private String clientAlias;

    //voice sender properties
    private String callType;
    private String textType;
    private Integer maxRetry;
    private String customerId;
    private String token;
    private String callerId;
    private String callFlowId;

    public boolean isUrlPresent(){
        return url != null;
    }
}