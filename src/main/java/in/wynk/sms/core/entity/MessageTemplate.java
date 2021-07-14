package in.wynk.sms.core.entity;

import in.wynk.data.entity.MongoBaseEntity;
import in.wynk.sms.enums.CommunicationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@ToString
@SuperBuilder
@Document(collection = "message_template")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageTemplate extends MongoBaseEntity<String> {
    private int linkedHeaders;
    private String templateName;
    private String templateContent;
    private CommunicationType communicationType;
    private String templateStatus;
    private String templateRegistered;
}
