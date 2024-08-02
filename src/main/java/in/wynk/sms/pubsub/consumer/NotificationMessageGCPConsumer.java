package in.wynk.sms.pubsub.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import in.wynk.pubsub.extractor.IPubSubMessageExtractor;
import in.wynk.pubsub.poller.AbstractPubSubMessagePolling;
import in.wynk.sms.common.message.SmsNotificationGCPMessage;
import in.wynk.sms.event.SmsNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationMessageGCPConsumer extends AbstractPubSubMessagePolling<SmsNotificationGCPMessage> {

    @Value("${sms.notification.pubSub.consumer.enabled}")
    private boolean enabled;

    @Value("${sms.notification.pubSub.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.notification.pubSub.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;
    private final ExecutorService messageHandlerThreadPool;
    private final ScheduledExecutorService pollingThreadPool;

    public NotificationMessageGCPConsumer(String projectName, String topicName, String subscriptionName,
                                          ExecutorService messageHandlerThreadPool,
                                          ObjectMapper objectMapper,
                                          ScheduledExecutorService pollingThreadPool) {
        super(projectName, topicName, subscriptionName, messageHandlerThreadPool, pollingThreadPool,objectMapper);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Override
    @AnalyseTransaction(name = "consumeNotificationMessage")
    public void consume(SmsNotificationGCPMessage message) {
        eventPublisher.publishEvent(SmsNotificationEvent.builder()
                .messageId(message.getMessageId())
                .msisdn(message.getMsisdn())
                .message(message.getMessage())
                .service(message.getService())
                .priority(message.getPriority().getSmsPriority())
                .contextMap(message.getContextMap())
                .build());

    }

    @Override
    public Class<SmsNotificationGCPMessage> messageType() {
        return SmsNotificationGCPMessage.class;
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting...");
            /*pollingThreadPool.scheduleWithFixedDelay(
                    this::poll,
                    0,
                    consumerDelay,
                    delayTimeUnit
            );*/
        }

    }

    @Override
    public void stop() {
        if (enabled) {
            log.info("Shutting down ...");
            pollingThreadPool.shutdownNow();
            messageHandlerThreadPool.shutdown();
        }
    }
}
