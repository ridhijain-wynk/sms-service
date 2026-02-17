package in.wynk.sms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import static in.wynk.stream.constant.StreamMarker.KAFKA_CONSUMPTION_ERROR;

@Slf4j
public class WhatsappKafkaDeserializer implements Deserializer<AbstractWhatsappOutboundMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AbstractWhatsappOutboundMessage deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, AbstractWhatsappOutboundMessage.class);
        } catch (Exception e) {
            log.error(KAFKA_CONSUMPTION_ERROR, "Error in deserializing the payload {}", e.getMessage(), e);
        }
        return null;
    }
}