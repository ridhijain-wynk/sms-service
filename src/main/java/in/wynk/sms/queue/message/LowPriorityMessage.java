package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@WynkQueue(queueName = "${sms.priority.low.priority.queue.name}", delaySeconds = "${sms.priority.low.priority.queue.delayInSecond}")
public class LowPriorityMessage extends SmsRequest {

    @Override
    public SMSPriority priority() {
        return SMSPriority.LOW;
    }
}
