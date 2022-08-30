package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import in.wynk.sms.enums.CommunicationType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
@SuperBuilder
@Document(collection = "messages")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AnalysedEntity
public class Messages extends MongoBaseEntity<String> {
    private String message;
    private String priority;
    private boolean enabled;
    private String sender;
    private String linkedHeader;
    @Field("template_id")
    private String templateId;
    @Field("template_name")
    private String templateName;
    private CommunicationType communicationType;
    private String templateStatus;
    private String templateRegistered;
    private boolean variablesPresent;
}
