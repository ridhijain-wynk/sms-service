package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;

@WynkQueue(queueName = "${sms.low.priority.queue.name}", delaySeconds = "${sms.low.priority.queue.delayInSecond}")
public class LowPriorityMessage {

}
