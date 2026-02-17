package in.wynk.sms.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import static in.wynk.stream.constant.StreamMarker.KAFKA_CONSUMPTION_ERROR;

@Slf4j
public class MediumPriorityKafkaDeserializer implements Deserializer<MediumPriorityMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MediumPriorityMessage deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, MediumPriorityMessage.class);
        } catch (Exception e) {
            log.error(KAFKA_CONSUMPTION_ERROR, "Error in deserializing the payload {}", e.getMessage(), e);
        }
        return null;
    }
}