package in.wynk.sms.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.model.SQSQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static in.wynk.sms.constants.SmsMarkers.SQS_CONFIG_ERROR;

/**
 * This class contains the methods for the configuration of the AWS SQS for the three queues for high, medium and low priority
 * messages.
 *
 */
@Configuration
public class SQSConfig {

    private static final Logger logger = LoggerFactory.getLogger(SQSConfig.class);

    private AmazonSQS sqs;

    @Value("${high.priority.queue}")
    private String highPriorityQueue;

    @Value("${medium.priority.queue}")
    private String mediumPriorityQueue;

    @Value("${low.priority.queue}")
    private String lowPriorityQueue;

    @Value("${promotional.msg.queue}")
    private String promotionalQueue;

    @Value("${notification.msg.queue}")
    private String notificationQueue;

    private static String highPriorityQueueUrl;
    private static String mediumPriorityQueueUrl;
    private static String lowPriorityQueueUrl;
    private static String promotionalQueueUrl;
    private static String notificationQueueUrl;

    /**
     * Initializes the SQS client and then fetches respective sqs queue URLs.
     */
    @PostConstruct
    private void init() {
        sqs = AmazonSQSClientBuilder.defaultClient();
        initializeQueueUrl();
    }

    /**
     * Fetches and saves the queue url for respective queues.
     */
    private void initializeQueueUrl() {
        highPriorityQueueUrl = sqs.getQueueUrl(highPriorityQueue).getQueueUrl();
        mediumPriorityQueueUrl = sqs.getQueueUrl(mediumPriorityQueue).getQueueUrl();
        lowPriorityQueueUrl = sqs.getQueueUrl(lowPriorityQueue).getQueueUrl();
        promotionalQueueUrl = sqs.getQueueUrl(promotionalQueue).getQueueUrl();
        notificationQueueUrl = sqs.getQueueUrl(notificationQueue).getQueueUrl();
    }

    /**
     * Returns the SQS client which is initialised at startup.
     *
     * @return the aws sqs client.
     */
    public AmazonSQS getSqs() {
        return sqs;
    }

    /**
     * Returns the queue details{@link SQSQueue} according to the priority. The name of the queue as well as the queue url is
     * returned as response.
     *
     * @param smsPriority the priority of the sms for which the corresponding queue details need to be fetched.
     * @return the queue details of the queue corresponding to the priority queried.
     */
    public SQSQueue getQueue(String smsPriority) {
        String queueName = StringUtils.EMPTY;
        String queueUrl = StringUtils.EMPTY;
        SMSPriority priority1 = SMSPriority.valueOf(smsPriority);
        switch (priority1) {
            case HIGH:
                queueName = highPriorityQueue;
                queueUrl = highPriorityQueueUrl;
                break;
            case MEDIUM:
                queueName = mediumPriorityQueue;
                queueUrl = mediumPriorityQueueUrl;
                break;
            case LOW:
                queueName = lowPriorityQueue;
                queueUrl = lowPriorityQueueUrl;
                break;
        }
        if (StringUtils.isEmpty(queueUrl)) {
            try {
                queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
                initializeQueueUrl();
            } catch (Exception e) {
                logger.error(SQS_CONFIG_ERROR, "Unable to get queue url for queue name : {}", queueName, e);
                throw e;
            }
        }
        return new SQSQueue(queueName, queueUrl);
    }

    public SQSQueue getPromotionalMessageQueue(){
        return new SQSQueue(promotionalQueue, promotionalQueueUrl);
    }

    public SQSQueue getNotificationMessageQueue(){
        return new SQSQueue(notificationQueue, notificationQueueUrl);
    }
}
