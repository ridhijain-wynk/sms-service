package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

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
}
