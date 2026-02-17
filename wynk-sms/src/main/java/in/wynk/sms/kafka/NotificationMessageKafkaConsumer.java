package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.core.service.ISenderHandler;
import in.wynk.sms.dto.MessageDetails;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.event.SmsNotificationEvent;
import in.wynk.sms.queue.message.MediumPriorityMessage;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@DependsOn("kafkaConsumerConfig")
public class NotificationMessageKafkaConsumer extends AbstractKafkaEventConsumer<String, SmsNotificationMessage> {

    @Value("${wynk.kafka.consumers.enabled}")
    private boolean enabled;
    private final KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public NotificationMessageKafkaConsumer (KafkaListenerEndpointRegistry endpointRegistry) {
        super();
        this.endpointRegistry = endpointRegistry;
    }

    @Override
    @AnalyseTransaction(name = "consumeNotificationMessage")
    public void consume(SmsNotificationMessage message) {
        eventPublisher.publishEvent(SmsNotificationEvent.builder()
                .messageId(message.getMessageId())
                .msisdn(message.getMsisdn())
                .message(message.getMessage())
                .service(message.getService())
                .priority(message.getPriority().getSmsPriority())
                .contextMap(message.getContextMap())
                .build());
    }

    @KafkaListener(id = "notificationMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.notification[0].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.notification[0].name}")
    protected void listenNotificationMessage(@Header(value = StreamConstant.MESSAGE_LAST_ATTEMPTED_SEQUENCE, required = false) String lastAttemptedSequence,
                                             @Header(value = StreamConstant.MESSAGE_CREATION_DATETIME, required = false) String createdAt,
                                             @Header(value = StreamConstant.MESSAGE_LAST_PROCESSED_DATETIME, required = false) String lastProcessedAt,
                                             @Header(value = StreamConstant.RETRY_COUNT, required = false) String retryCount,
                                               ConsumerRecord<String, SmsNotificationMessage> consumerRecord) {
        try {
            log.debug("Kafka consume record result {} for event {}", consumerRecord, consumerRecord.value().toString());
            consume(consumerRecord.value());
        } catch (Exception e) {
            log.info("retrying kafka message {} with headers : lastAttemptedSequence {}, createdAt {}, lastProcessedAt {}, retryCount {}", consumerRecord.value(), lastAttemptedSequence, createdAt, lastProcessedAt, retryCount);
            if (!(e instanceof WynkRuntimeException)) {
                log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Something went wrong while processing message {} for kafka consumer : {}", consumerRecord.value(), ", SmsNotificationMessage - ", e);
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