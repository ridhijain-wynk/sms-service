package in.wynk.sms.queue.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.sms.queue.message.LowPriorityMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LowPriorityConsumer extends AbstractSQSMessageConsumerPollingQueue<LowPriorityMessage> {

    @Value("${sms.priority.low.queue.consumer.enabled}")
    private boolean enabled;
    @Value("${sms.priority.low.queue.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.priority.low.queue.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;

    private final ThreadPoolExecutor messageHandlerThreadPool;
    private final ScheduledThreadPoolExecutor pollingThreadPool;

    public LowPriorityConsumer(String queueName,
                               AmazonSQS sqs,
                               ISQSMessageExtractor messagesExtractor,
                               ThreadPoolExecutor messageHandlerThreadPool,
                               ScheduledThreadPoolExecutor pollingThreadPool) {
        super(queueName, sqs, messagesExtractor, messageHandlerThreadPool);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Override
    @AnalyseTransaction(name = "consumeMessage")
    public void consume(LowPriorityMessage message) {
        AnalyticService.update(message);
        //TODO: write consumer logic
    }

    @Override
    public Class<LowPriorityMessage> messageType() {
        return LowPriorityMessage.class;
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting PaymentReconciliationConsumerPollingQueue...");
            pollingThreadPool.scheduleWithFixedDelay(
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
            log.info("Shutting down PaymentReconciliationConsumerPollingQueue ...");
            pollingThreadPool.shutdownNow();
            messageHandlerThreadPool.shutdown();
        }
    }

}
