package in.wynk.sms.pubsub.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.pubsub.extractor.IPubSubMessageExtractor;
import in.wynk.pubsub.poller.AbstractPubSubMessagePolling;
import in.wynk.sms.core.service.ISenderHandler;
import in.wynk.sms.dto.MessageDetails;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.pubsub.message.MediumPriorityGCPMessage;
import in.wynk.sms.sender.IMessageSender;
import in.wynk.sms.sender.ISmsSenderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static in.wynk.sms.constants.SmsLoggingMarkers.MEDIUM_PRIORITY_SMS_ERROR;

@Slf4j
public class MediumPriorityGCPConsumer  extends AbstractPubSubMessagePolling<MediumPriorityGCPMessage> {

    @Value("${sms.priority.medium.pubSub.consumer.enabled}")
    private boolean enabled;

    @Value("${sms.priority.medium.pubSub.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.priority.medium.pubSub.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;

    private final ExecutorService messageHandlerThreadPool;
    private final ScheduledExecutorService pollingThreadPool;

    public MediumPriorityGCPConsumer(String projectName, String topicName, String subscriptionName,
                                      ExecutorService messageHandlerThreadPool,
                                      ObjectMapper objectMapper,
                                      ScheduledExecutorService pollingThreadPool) {
        super(projectName, topicName, subscriptionName, messageHandlerThreadPool, pollingThreadPool, objectMapper);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Autowired
    private ISmsSenderUtils smsSenderUtils;

    @Autowired
    private ISenderHandler senderHandler;
    @Override
    public void consume(MediumPriorityGCPMessage message) {
        try {
            Map<String, IMessageSender<SmsRequest>> senderMap = smsSenderUtils.fetchSmsSender(message);
            senderHandler.handle(MessageDetails.builder().senderMap(senderMap).message(message).build());
        } catch (Exception e) {
            log.error(MEDIUM_PRIORITY_SMS_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public Class<MediumPriorityGCPMessage> messageType() {
        return MediumPriorityGCPMessage.class;
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
