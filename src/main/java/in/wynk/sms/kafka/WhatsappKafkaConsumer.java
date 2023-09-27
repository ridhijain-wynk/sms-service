package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.dto.request.whatsapp.WhatsappSendMessage;
import in.wynk.stream.constant.StreamMarker;
import in.wynk.stream.consumer.impl.AbstractKafkaEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@DependsOn("kafkaConsumerConfig")
public class WhatsappKafkaConsumer extends AbstractKafkaEventConsumer<String, WhatsappSendMessage> {

    private final IWhatsappKafkaHandler<WhatsappSendMessage> whatsappKafkaHandler;

    @Value("${wynk.kafka.consumers.enabled}")
    private boolean enabled;

    private final KafkaListenerEndpointRegistry endpointRegistry;

    public WhatsappKafkaConsumer (IWhatsappKafkaHandler<WhatsappSendMessage> whatsappKafkaHandler, KafkaListenerEndpointRegistry endpointRegistry) {
        super();
        this.whatsappKafkaHandler = whatsappKafkaHandler;
        this.endpointRegistry = endpointRegistry;
    }

    @Override
    public void consume(WhatsappSendMessage message) throws WynkRuntimeException {
        whatsappKafkaHandler.sendMessage(message);
    }

    //@Retryable
    @KafkaListener(id = "whatsappSendMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.whatsapp[0].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.whatsapp[0].name}")
    @AnalyseTransaction(name = "whatsappSendMessage")
    protected void listenGenerateInvoice(ConsumerRecord<String, WhatsappSendMessage> consumerRecord) {
        try {
            log.debug("Kafka consume record result {} for event {}", consumerRecord, consumerRecord.value().toString());
            consume(consumerRecord.value());
        } catch (Exception e) {
            log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Error occurred in polling/consuming kafka event", e);
        }
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting Kafka consumption...");
        }
    }

    @Override
    public void stop() {
        if (enabled) {
            log.info("Shutting down Kafka consumption...");
            this.endpointRegistry.stop();
        }
    }
}