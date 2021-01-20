package in.wynk.sms.queue.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationMessageConsumer extends AbstractSQSMessageConsumerPollingQueue<SmsNotificationMessage> {

    @Value("${sms.notification.queue.consumer.enabled}")
    private boolean enabled;
    @Value("${sms.notification.queue.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.notification.queue.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;

    private final ThreadPoolExecutor messageHandlerThreadPool;
    private final ScheduledThreadPoolExecutor pollingThreadPool;

    public NotificationMessageConsumer(String queueName,
                                       AmazonSQS sqs,
                                       ObjectMapper objectMapper,
                                       ISQSMessageExtractor messagesExtractor,
                                       ThreadPoolExecutor messageHandlerThreadPool,
                                       ScheduledThreadPoolExecutor pollingThreadPool) {
        super(queueName, sqs, objectMapper, messagesExtractor, messageHandlerThreadPool);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Autowired
    private ISqsManagerService sqsManagerService;

    @Override
    @AnalyseTransaction(name = "consumeNotificationMessage")
    public void consume(SmsNotificationMessage message) {
        AnalyticService.update(message);
        SmsRequest smsRequest = SMSFactory.getSmsRequest(message);
        sqsManagerService.publishSQSMessage(smsRequest);
    }

    @Override
    public Class<SmsNotificationMessage> messageType() {
        return SmsNotificationMessage.class;
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting...");
            pollingThreadPool.scheduleAtFixedRate(
                    this::poll,
                    0,
                    consumerDelay,
                    delayTimeUnit
            );
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
