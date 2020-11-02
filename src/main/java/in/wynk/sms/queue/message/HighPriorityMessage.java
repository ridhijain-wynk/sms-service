package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;
import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@WynkQueue(queueName = "${sms.priority.high.priority.queue.name}", delaySeconds = "${sms.priority.high.priority.queue.delayInSecond}")
public class HighPriorityMessage extends SmsRequest {


    @Override
    public SMSPriority priority() {
        return SMSPriority.HIGH;
    }
}
