package in.wynk.sms.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import in.wynk.queue.constant.BeanConstant;
import in.wynk.sms.queue.consumer.HighPriorityConsumer;
import in.wynk.sms.queue.consumer.LowPriorityConsumer;
import in.wynk.sms.queue.consumer.MediumPriorityConsumer;
import in.wynk.sms.queue.extractor.SmsMessageExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class SmsSqsConfig {

    @Bean
    public HighPriorityConsumer highPriorityConsumer(@Value("${sms.priority.high.queue.name}") String queueName,
                                                     @Value("${sms.priority.high.queue.threads.parallelism:5}") int parallelism,
                                                     @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient) {
        return new HighPriorityConsumer(queueName,
                sqsClient,
                new SmsMessageExtractor(queueName, sqsClient),
                (ThreadPoolExecutor) threadPoolExecutor(parallelism),
                (ScheduledThreadPoolExecutor) scheduledThreadPoolExecutor());
    }

    @Bean
    public LowPriorityConsumer lowPriorityConsumer(@Value("${sms.priority.low.queue.name}") String queueName,
                                                   @Value("${sms.priority.low.queue.threads.parallelism:5}") int parallelism,
                                                   @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient) {
        return new LowPriorityConsumer(queueName,
                sqsClient,
                new SmsMessageExtractor(queueName, sqsClient),
                (ThreadPoolExecutor) threadPoolExecutor(parallelism),
                (ScheduledThreadPoolExecutor) scheduledThreadPoolExecutor());
    }

    @Bean
    public MediumPriorityConsumer mediumPriorityConsumer(@Value("${sms.priority.low.queue.name}") String queueName,
                                                      @Value("${sms.priority.low.queue.threads.parallelism:5}") int parallelism,
                                                      @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient) {
        return new MediumPriorityConsumer(queueName,
                sqsClient,
                new SmsMessageExtractor(queueName, sqsClient),
                (ThreadPoolExecutor) threadPoolExecutor(parallelism),
                (ScheduledThreadPoolExecutor) scheduledThreadPoolExecutor());
    }

    private ExecutorService threadPoolExecutor(int parallelism) {
        return Executors.newWorkStealingPool(parallelism);
    }

    private ScheduledExecutorService scheduledThreadPoolExecutor() {
        return Executors.newScheduledThreadPool(2);
    }

}
