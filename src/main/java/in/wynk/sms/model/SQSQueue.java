package in.wynk.sms.model;

/**
 * This details of the SQS queue such as name of the queue, the url associated with the queue name.
 *
 * @author Abhishek
 * @created 09/10/19
 */
public class SQSQueue {

    private String queueName;
    private String queueUrl;

    public SQSQueue(String queueName, String queueUrl) {
        this.queueName = queueName;
        this.queueUrl = queueUrl;
    }

    public SQSQueue() {
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }
}
