package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.dto.request.whatsapp.TextMessageRequest;
import in.wynk.sms.dto.request.whatsapp.WhatsappMessageRequest;
import in.wynk.sms.dto.request.whatsapp.WhatsappSendMessage;
import in.wynk.sms.dto.response.WhatsappMessageResponse;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.service.WhatsappManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Service
@Slf4j
public class WhatsappKafkaConsumptionHandler implements IWhatsappKafkaHandler<WhatsappSendMessage> {

    private final WhatsappManagerService whatsappManager;

    public WhatsappKafkaConsumptionHandler (WhatsappManagerService whatsappManager) {
        this.whatsappManager = whatsappManager;
    }

    @Override
    public void sendMessage(WhatsappSendMessage message) {
        try {
            AnalyticService.update(message);
            if (ObjectUtils.isEmpty(message) || Objects.isNull(message.getMessageType()) || Objects.isNull(message.getMessageRequest())) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS001);
            }
            //todo :  transform whatsapp request from message
            whatsappManager.send(TextMessageRequest.builder().build());
        } catch (Exception ex) {
            log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED, ex.getMessage(), ex);
        }
    }
}