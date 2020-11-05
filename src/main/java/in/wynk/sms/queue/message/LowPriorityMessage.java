package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@WynkQueue(queueName = "${sms.priority.low.queue.name}", delaySeconds = "${sms.priority.low.queue.delayInSecond}")
public class LowPriorityMessage extends SmsRequest {

    @Builder.Default
    private final SMSPriority priority = SMSPriority.LOW;
}
