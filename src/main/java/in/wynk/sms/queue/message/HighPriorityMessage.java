package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;

@WynkQueue(queueName = "${sms.high.priority.queue.name}", delaySeconds = "${sms.high.priority.queue.delayInSecond}")
public class HighPriorityMessage {

}
