package in.wynk.sms.queue.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import in.wynk.sms.sender.AbstractSMSSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MediumPriorityConsumer extends AbstractSQSMessageConsumerPollingQueue<MediumPriorityMessage> {

    @Value("${sms.priority.medium.queue.consumer.enabled}")
    private boolean enabled;
    @Value("${sms.priority.medium.queue.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.priority.medium.queue.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;

    private final ExecutorService messageHandlerThreadPool;
    private final ScheduledExecutorService pollingThreadPool;

    public MediumPriorityConsumer(String queueName,
                                  AmazonSQS sqs,
                                  ObjectMapper objectMapper,
                                  ISQSMessageExtractor messagesExtractor,
                                  ExecutorService messageHandlerThreadPool,
                                  ScheduledExecutorService pollingThreadPool) {
        super(queueName, sqs, objectMapper, messagesExtractor, messageHandlerThreadPool);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Autowired
    private AbstractSMSSender smsSender;

    @Override
    @AnalyseTransaction(name = "consumeMessage")
    public void consume(MediumPriorityMessage message) {
        AnalyticService.update(message);
        smsSender.sendMessage(message);
    }

    @Override
    public Class<MediumPriorityMessage> messageType() {
        return MediumPriorityMessage.class;
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting ...");
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
            log.info("Shutting down ...");
            pollingThreadPool.shutdownNow();
            messageHandlerThreadPool.shutdown();
        }
    }

}
