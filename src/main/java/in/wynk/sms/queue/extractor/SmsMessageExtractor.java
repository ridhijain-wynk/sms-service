package in.wynk.sms.queue.extractor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import in.wynk.queue.extractor.AbstractSQSMessageExtractor;

public class SmsMessageExtractor extends AbstractSQSMessageExtractor {

    private int batchSize;
    private int waitTimeInSeconds;
    private int visibilityTimeoutSeconds;
    private final String queueName;


    public SmsMessageExtractor(String queueName, AmazonSQS sqs, int batchSize, int waitTimeInSeconds, int visibilityTimeoutSeconds) {
        super(sqs);
        this.queueName = queueName;
        this.batchSize = batchSize;
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
        this.waitTimeInSeconds = waitTimeInSeconds;
    }

    @Override
    public ReceiveMessageRequest buildReceiveMessageRequest() {
        return new ReceiveMessageRequest()
                .withMaxNumberOfMessages(batchSize)
                .withQueueUrl(getSqs().getQueueUrl(queueName).getQueueUrl())
                .withVisibilityTimeout(visibilityTimeoutSeconds)
                .withWaitTimeSeconds(waitTimeInSeconds);
    }
}
