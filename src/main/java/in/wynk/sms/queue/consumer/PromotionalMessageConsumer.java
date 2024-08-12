package in.wynk.sms.queue.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.queue.extractor.ISQSMessageExtractor;
import in.wynk.queue.poller.AbstractSQSMessageConsumerPollingQueue;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.stream.producer.IKafkaPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static in.wynk.sms.constants.SmsLoggingMarkers.PROMOTIONAL_MSG_ERROR;

@Slf4j
public class PromotionalMessageConsumer extends AbstractSQSMessageConsumerPollingQueue<SendSmsRequest[]> {

    private final ExecutorService messageHandlerThreadPool;
    private final ScheduledExecutorService pollingThreadPool;
    @Value("${sms.promotional.queue.consumer.enabled}")
    private boolean enabled;
    @Value("${sms.promotional.queue.consumer.delay}")
    private long consumerDelay;
    @Value("${sms.promotional.queue.consumer.delayTimeUnit}")
    private TimeUnit delayTimeUnit;
    @Autowired
    private IKafkaPublisherService kafkaPublisherService;

    public PromotionalMessageConsumer(String queueName,
                                      AmazonSQS sqs,
                                      ObjectMapper objectMapper,
                                      ISQSMessageExtractor messagesExtractor,
                                      ExecutorService messageHandlerThreadPool,
                                      ScheduledExecutorService pollingThreadPool) {
        super(queueName, sqs, objectMapper, messagesExtractor, messageHandlerThreadPool);
        this.pollingThreadPool = pollingThreadPool;
        this.messageHandlerThreadPool = messageHandlerThreadPool;
    }

    @Override
    public void consume(SendSmsRequest[] requests) {
        for (SendSmsRequest request : requests) {
            if (request != null) {
                try {
                    SmsRequest message = parseMessage(request);
                    AnalyticService.update(message);
                    kafkaPublisherService.publishKafkaMessage(message);
                } catch (IllegalArgumentException ex) {
                    log.error(PROMOTIONAL_MSG_ERROR, "Invalid message: {} for msisdn: {}", request.getMessage(), request.getMsisdn());
                }
            }
        }
    }

    @AnalyseTransaction(name = "consumePromotionalMessage")
    private SmsRequest parseMessage(SendSmsRequest request) {
        SmsRequest smsRequest = SMSFactory.getSmsRequest(request);
        AnalyticService.update(smsRequest);
        return smsRequest;
    }

    @Override
    public Class<SendSmsRequest[]> messageType() {
        return SendSmsRequest[].class;
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting...");
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
