package in.wynk.sms.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.queue.message.LowPriorityMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import static in.wynk.stream.constant.StreamMarker.KAFKA_CONSUMPTION_ERROR;

@Slf4j
public class LowPriorityKafkaDeserializer implements Deserializer<LowPriorityMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LowPriorityMessage deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, LowPriorityMessage.class);
        } catch (Exception e) {
            log.error(KAFKA_CONSUMPTION_ERROR, "Error in deserializing the payload {}", e.getMessage(), e);
        }
        return null;
    }
}