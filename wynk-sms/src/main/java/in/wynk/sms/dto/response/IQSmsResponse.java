package in.wynk.sms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@AnalysedEntity
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class IQSmsResponse {
     String customerId;
     String messageRequestId;
     String sourceAddress;
     String message;
     String messageType;
     String dltTemplateId;
     String entityId;
     List<String> destinationAddress;
     List<String> incorrectNum;
}
