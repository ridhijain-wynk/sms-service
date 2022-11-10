package in.wynk.sms.event;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinpointStreamEvent {

    @Analysed
    @Field("eventType")
    private String event_type;
    @Analysed
    @Field("eventTimestamp")
    private long event_timestamp;
    @Analysed
    @Field("arrivalTimestamp")
    private long arrival_timestamp;
    @Field("eventVersion")
    private String event_version;
    @Analysed
    private Map<String, Object> application;
    private Map<String, String> client;
    private Map<String, Object> device;
    private Object session;
    @Analysed
    private Map<String, String> attributes;
    @Analysed
    private Map<String, Double> metrics;
    @Analysed
    private String awsAccountId;
}
