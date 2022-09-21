package in.wynk.sms.event;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationEvent {

    @Analysed
    private String service;
    @Analysed
    private String msisdn;
    @Analysed
    private String message;
    @Analysed
    private String priority;
    @Analysed
    private String messageId;
    private Map<String, Object> contextMap;
    @Analysed
    private String sender;
}
