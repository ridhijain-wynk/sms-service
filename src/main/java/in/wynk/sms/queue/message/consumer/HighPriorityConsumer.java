package in.wynk.sms.queue.message.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.sms.queue.message.HighPriorityMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HighPriorityConsumer extends AbstractSQSMessageConsumerPollingQueue<HighPriorityMessage> {

    @Value("${sms.high.queue.consumer.enabled}")
    private boolean enabled;
    @Value("${sms.high.queue.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.high.queue.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;

    private final ThreadPoolExecutor messageHandlerThreadPool;
    private final ScheduledThreadPoolExecutor pollingThreadPool;

    public HighPriorityConsumer(String queueName,
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
    public void consume(HighPriorityMessage message) {
        AnalyticService.update(message);
        //TODO: write consumer logic
    }

    @Override
    public Class<HighPriorityMessage> messageType() {
        return HighPriorityMessage.class;
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
