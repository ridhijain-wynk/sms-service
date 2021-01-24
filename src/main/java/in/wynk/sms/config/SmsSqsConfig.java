package in.wynk.sms.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.queue.constant.BeanConstant;
import in.wynk.sms.queue.consumer.*;
import in.wynk.sms.queue.extractor.SmsMessageExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SmsSqsConfig {

    @Value("${sms.sqs.messages.schedule.thread.pool.size:10}")
    private int schedulerPoolSize;
    @Value("${sms.sqs.messages.extractor.batchSize:100}")
    private int batchSize;
    @Value("${sms.sqs.messages.extractor.waitTimeInSeconds:1}")
    private int waitTimeSeconds;
    @Value("${sms.sqs.messages.extractor.visibilityTimeoutSeconds:30}")
    private int visibilityTimeoutSeconds;

    @Bean
    public NotificationMessageConsumer notificationConsumer(@Value("${sms.notification.queue.name}") String queueName,
                                                            @Value("${sms.notification.queue.threads:5}") int parallelism,
                                                            @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient,
                                                            ObjectMapper objectMapper) {
        return new NotificationMessageConsumer(queueName,
                sqsClient,
                objectMapper,
                new SmsMessageExtractor(queueName, sqsClient, batchSize, waitTimeSeconds, visibilityTimeoutSeconds),
                executor(parallelism),
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public PromotionalMessageConsumer promotionalConsumer(@Value("${sms.promotional.queue.name}") String queueName,
                                                          @Value("${sms.promotional.queue.threads:5}") int parallelism,
                                                          @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient,
                                                          ObjectMapper objectMapper) {
        return new PromotionalMessageConsumer(queueName,
                sqsClient,
                objectMapper,
                new SmsMessageExtractor(queueName, sqsClient, batchSize, waitTimeSeconds, visibilityTimeoutSeconds),
                executor(parallelism),
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public HighPriorityConsumer highPriorityConsumer(@Value("${sms.priority.high.queue.name}") String queueName,
                                                     @Value("${sms.priority.high.queue.threads:5}") int parallelism,
                                                     @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient,
                                                     ObjectMapper objectMapper) {
        return new HighPriorityConsumer(queueName,
                sqsClient,
                objectMapper,
                new SmsMessageExtractor(queueName, sqsClient, batchSize, waitTimeSeconds, visibilityTimeoutSeconds),
                executor(parallelism),
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public LowPriorityConsumer lowPriorityConsumer(@Value("${sms.priority.low.queue.name}") String queueName,
                                                   @Value("${sms.priority.low.queue.threads:5}") int parallelism,
                                                   @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient,
                                                   ObjectMapper objectMapper) {
        return new LowPriorityConsumer(queueName,
                sqsClient,
                objectMapper,
                new SmsMessageExtractor(queueName, sqsClient, batchSize, waitTimeSeconds, visibilityTimeoutSeconds),
                executor(parallelism),
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public MediumPriorityConsumer mediumPriorityConsumer(@Value("${sms.priority.medium.queue.name}") String queueName,
                                                         @Value("${sms.priority.medium.queue.threads:5}") int parallelism,
                                                         @Qualifier(BeanConstant.SQS_MANAGER) AmazonSQS sqsClient,
                                                         ObjectMapper objectMapper) {
        return new MediumPriorityConsumer(queueName,
                sqsClient,
                objectMapper,
                new SmsMessageExtractor(queueName, sqsClient, batchSize, waitTimeSeconds, visibilityTimeoutSeconds),
                executor(parallelism),
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    private ExecutorService executor(int threads) {
        return Executors.newCachedThreadPool();
    }

    private ScheduledExecutorService scheduledThreadPoolExecutor(int schedulerPoolSize) {
        return Executors.newScheduledThreadPool(schedulerPoolSize);
    }

}
