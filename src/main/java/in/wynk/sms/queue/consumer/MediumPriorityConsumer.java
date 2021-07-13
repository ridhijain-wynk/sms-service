package in.wynk.sms.queue.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import in.wynk.sms.sender.AbstractSMSSender;
import in.wynk.sms.sender.ISmsSenderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static in.wynk.sms.constants.SmsLoggingMarkers.MEDIUM_PRIORITY_SMS_ERROR;

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
    private ISmsSenderUtils smsSenderUtils;

    @Override
    public void consume(MediumPriorityMessage message) {
        try {
            AbstractSMSSender smsSender = smsSenderUtils.fetchSmsSender(message);
            smsSender.sendMessage(message);
        } catch (Exception e) {
            log.error(MEDIUM_PRIORITY_SMS_ERROR, e.getMessage(), e);
        }
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
