package in.wynk.sms.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.common.message.SmsNotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import static in.wynk.stream.constant.StreamMarker.KAFKA_CONSUMPTION_ERROR;

@Slf4j
public class NotificationMessageKafkaDeserializer implements Deserializer<SmsNotificationMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SmsNotificationMessage deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, SmsNotificationMessage.class);
        } catch (Exception e) {
            log.error(KAFKA_CONSUMPTION_ERROR, "Error in deserializing the payload {}", e.getMessage(), e);
        }
        return null;
    }
}