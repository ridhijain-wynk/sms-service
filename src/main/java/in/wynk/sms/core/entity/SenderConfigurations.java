package in.wynk.sms.core.entity;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.data.entity.MongoBaseEntity;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.dto.request.CommunicationType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Getter
@Setter
@ToString
@SuperBuilder
@Document(collection = "sender_configurations")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AnalysedEntity
public class SenderConfigurations extends MongoBaseEntity<String> {
    private String clientAlias;
    private Map<SMSPriority, Map<CommunicationType, SenderDetails>> details;
    @Field("scrubbing_enabled")
    private boolean scrubbingEnabled;
    @Field("country_code")
    private String countryCode;
}