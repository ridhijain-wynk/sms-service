
package in.wynk.sms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.pubsub.consumer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SmsPubSubConfig {

    @Value("${sms.pubSub.messages.schedule.thread.pool.size:10}")
    private int schedulerPoolSize;

    @Bean
    public HighestPriorityGCPConsumer highestPriorityGCPConsumer(@Value("${sms.priority.highest.pubSub.projectName}") String projectName, @Value("${sms.priority.highest.pubSub.topicName}") String topicName, @Value("${sms.priority.highest.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.priority.highest.pubSub.threads:100}") int parallelism, ObjectMapper objectMapper, @Value("${sms.priority.highest.pubSub.bufferInterval}") String bufferInterval) {
        return new HighestPriorityGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public HighPriorityGCPConsumer highPriorityConsumerPubSub(@Value("${sms.priority.high.pubSub.projectName}") String projectName, @Value("${sms.priority.high.pubSub.topicName}") String topicName, @Value("${sms.priority.high.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.priority.high.pubSub.threads:100}") int parallelism, ObjectMapper objectMapper, @Value("${sms.priority.high.pubSub.bufferInterval}") String bufferInterval) {
        return new HighPriorityGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public MediumPriorityGCPConsumer mediumPriorityGCPConsumer(@Value("${sms.priority.medium.pubSub.projectName}") String projectName, @Value("${sms.priority.medium.pubSub.topicName}") String topicName, @Value("${sms.priority.medium.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.priority.medium.pubSub.threads:5}") int parallelism, ObjectMapper objectMapper, @Value("${sms.priority.medium.pubSub.bufferInterval}") String bufferInterval) {
        return new MediumPriorityGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public LowPriorityGCPConsumer lowPriorityGCPConsumer(@Value("${sms.priority.low.pubSub.projectName}") String projectName, @Value("${sms.priority.low.pubSub.topicName}") String topicName, @Value("${sms.priority.low.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.priority.low.pubSub.threads:5}") int parallelism, ObjectMapper objectMapper, @Value("${sms.priority.low.pubSub.bufferInterval}") String bufferInterval) {
        return new LowPriorityGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public NotificationMessageGCPConsumer notificationMessageGCPConsumer(@Value("${sms.notification.pubSub.projectName}") String projectName, @Value("${sms.notification.pubSub.topicName}") String topicName, @Value("${sms.notification.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.notification.pubSub.threads:80}") int parallelism, ObjectMapper objectMapper, @Value("${sms.notification.pubSub.bufferInterval}") String bufferInterval) {
        return new NotificationMessageGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    @Bean
    public PromotionalMessageGCPConsumer promotionalMessageGCPConsumer(@Value("${sms.promotional.pubSub.projectName}") String projectName, @Value("${sms.promotional.pubSub.topicName}") String topicName, @Value("${sms.promotional.pubSub.subscriptionName}") String subscriptionName, @Value("${sms.promotional.pubSub.threads:5}") int parallelism, ObjectMapper objectMapper, @Value("${sms.notification.pubSub.bufferInterval}") String bufferInterval) {
        return new PromotionalMessageGCPConsumer(projectName, topicName, subscriptionName,
                executor(parallelism),
                objectMapper,
                scheduledThreadPoolExecutor(schedulerPoolSize));
    }

    private ExecutorService executor(int threads) {
        return Executors.newCachedThreadPool();
    }


    private ScheduledExecutorService scheduledThreadPoolExecutor(int schedulerPoolSize) {
        return Executors.newScheduledThreadPool(schedulerPoolSize);
    }
}

