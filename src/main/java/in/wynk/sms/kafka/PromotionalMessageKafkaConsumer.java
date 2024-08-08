package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.event.SmsNotificationEvent;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.stream.constant.StreamConstant;
import in.wynk.stream.constant.StreamMarker;
import in.wynk.stream.consumer.impl.AbstractKafkaEventConsumer;
import in.wynk.stream.producer.IKafkaPublisherService;
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

import static in.wynk.sms.constants.SmsLoggingMarkers.PROMOTIONAL_MSG_ERROR;


@Slf4j
@Service
@DependsOn("kafkaConsumerConfig")
public class PromotionalMessageKafkaConsumer extends AbstractKafkaEventConsumer<String, SendSmsRequest[]> {

    @Value("${wynk.kafka.consumers.enabled}")
    private boolean enabled;
    private final KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private IKafkaPublisherService kafkaPublisherService;

    public PromotionalMessageKafkaConsumer (KafkaListenerEndpointRegistry endpointRegistry) {
        super();
        this.endpointRegistry = endpointRegistry;
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

    @KafkaListener(id = "notificationMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.notification[0].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.notification[0].name}")
    protected void listenNotificationMessage(@Header(StreamConstant.MESSAGE_LAST_ATTEMPTED_SEQUENCE) String lastAttemptedSequence,
                                             @Header(StreamConstant.MESSAGE_CREATION_DATETIME) String createdAt,
                                             @Header(StreamConstant.MESSAGE_LAST_PROCESSED_DATETIME) String lastProcessedAt,
                                             @Header(StreamConstant.RETRY_COUNT) String retryCount,
                                             ConsumerRecord<String, SendSmsRequest[]> consumerRecord) {
        try {
            log.debug("Kafka consume record result {} for event {}", consumerRecord, consumerRecord.value().toString());
            consume(consumerRecord.value());
        } catch (Exception e) {
            log.info("retrying kafka message {} with headers : lastAttemptedSequence {}, createdAt {}, lastProcessedAt {}, retryCount {}", consumerRecord.value(), lastAttemptedSequence, createdAt, lastProcessedAt, retryCount);
            if (!(e instanceof WynkRuntimeException)) {
                log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Something went wrong while processing message {} for kafka consumer : {}", consumerRecord.value(), ", SendSmsRequest - ", e);
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