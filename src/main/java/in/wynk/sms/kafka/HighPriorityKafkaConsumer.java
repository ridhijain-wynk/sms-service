package in.wynk.sms.kafka;

import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.core.service.ISenderHandler;
import in.wynk.sms.dto.MessageDetails;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.sender.IMessageSender;
import in.wynk.sms.sender.ISmsSenderUtils;
import in.wynk.stream.constant.StreamConstant;
import in.wynk.stream.constant.StreamMarker;
import in.wynk.stream.consumer.impl.AbstractKafkaEventConsumer;
import in.wynk.stream.service.KafkaRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Map;

import static in.wynk.sms.constants.SmsLoggingMarkers.HIGH_PRIORITY_SMS_ERROR;

@Slf4j
@Service
@DependsOn("kafkaConsumerConfig")
public class HighPriorityKafkaConsumer extends AbstractKafkaEventConsumer<String, HighPriorityMessage> {

    @Value("${wynk.kafka.consumers.enabled}")
    private boolean enabled;
    private final KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private ISmsSenderUtils smsSenderUtils;
    @Autowired
    private ISenderHandler senderHandler;

    public HighPriorityKafkaConsumer (KafkaListenerEndpointRegistry endpointRegistry) {
        super();
        this.endpointRegistry = endpointRegistry;
    }

    @Override
    public void consume(HighPriorityMessage message) {
        try {
            log.info("Message consumed for request for "+ message.getMessageId()+ "- " + message.getMsisdn());
            Map<String, IMessageSender<SmsRequest>> senderMap = smsSenderUtils.fetchSmsSender(message);
            log.info("Senders fetched for request for "+ message.getMessageId()+ "- " + message.getMsisdn());
            senderHandler.handle(MessageDetails.builder().senderMap(senderMap).message(message).build());
        } catch (Exception e) {
            log.error(HIGH_PRIORITY_SMS_ERROR, e.getMessage(), e);
        }
    }

    @KafkaListener(id = "highPriorityMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.high[0].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.high[0].name}")
    protected void listenHighPriorityMessage(@Header(StreamConstant.MESSAGE_LAST_ATTEMPTED_SEQUENCE) String lastAttemptedSequence,
                                                @Header(StreamConstant.MESSAGE_CREATION_DATETIME) String createdAt,
                                                @Header(StreamConstant.MESSAGE_LAST_PROCESSED_DATETIME) String lastProcessedAt,
                                                @Header(StreamConstant.RETRY_COUNT) String retryCount,
                                                ConsumerRecord<String, HighPriorityMessage> consumerRecord) {
        try {
            log.debug("Kafka consume record result {} for event {}", consumerRecord, consumerRecord.value().toString());
            consume(consumerRecord.value());
        } catch (Exception e) {
            log.info("retrying kafka message {} with headers : lastAttemptedSequence {}, createdAt {}, lastProcessedAt {}, retryCount {}", consumerRecord.value(), lastAttemptedSequence, createdAt, lastProcessedAt, retryCount);
            if (!(e instanceof WynkRuntimeException)) {
                log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Something went wrong while processing message {} for kafka consumer : {}", consumerRecord.value(), ", HighPriorityMessage - ", e);
            }
        }
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting Kafka consumption..." + this.getClass().getCanonicalName());
        }
    }

    @Override
    public void stop() {
        if (enabled) {
            log.info("Shutting down Kafka consumption..." + this.getClass().getCanonicalName());
            this.endpointRegistry.stop();
        }
    }
}