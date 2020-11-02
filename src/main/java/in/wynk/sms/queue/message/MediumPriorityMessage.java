package in.wynk.sms.queue.message;

import in.wynk.queue.dto.WynkQueue;

@WynkQueue(queueName = "${sms.medium.priority.queue.name}", delaySeconds = "${sms.medium.priority.queue.delayInSecond}")
public class MediumPriorityMessage {

}
