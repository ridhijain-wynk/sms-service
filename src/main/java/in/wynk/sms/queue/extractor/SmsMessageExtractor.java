package in.wynk.sms.queue.extractor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import in.wynk.queue.constant.BeanConstant;
import in.wynk.queue.extractor.AbstractSQSMessageExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public class SmsMessageExtractor extends AbstractSQSMessageExtractor {

    @Value("${sms.sqs.messages.extractor.batchSize:100}")
    private int batchSize;

    @Value("${sms.sqs.messages.extractor.waitTimeInSeconds:1}")
    private int waitTimeInSeconds;

    private final String queueName;


    public SmsMessageExtractor(String queueName, @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqs) {
        super(sqs);
        this.queueName = queueName;
    }

    @Override
    public ReceiveMessageRequest buildReceiveMessageRequest() {
        return new ReceiveMessageRequest()
                .withMaxNumberOfMessages(batchSize)
                .withQueueUrl(getSqs().getQueueUrl(queueName).getQueueUrl())
                .withWaitTimeSeconds(waitTimeInSeconds);
    }
}
