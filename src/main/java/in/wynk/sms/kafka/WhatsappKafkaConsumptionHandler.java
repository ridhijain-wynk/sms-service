package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.common.dto.whatsapp.WhatsappMessageRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.service.WhatsappManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Service
@Slf4j
public class WhatsappKafkaConsumptionHandler implements IWhatsappKafkaHandler<WhatsappMessageRequest> {

    private final WhatsappManagerService whatsappManager;

    public WhatsappKafkaConsumptionHandler (WhatsappManagerService whatsappManager) {
        this.whatsappManager = whatsappManager;
    }

    @Override
    public void sendMessage(WhatsappMessageRequest request) {
        try {
            AnalyticService.update(request);
            if (ObjectUtils.isEmpty(request) || Objects.isNull(request.getClientAlias()) || Objects.isNull(request.getMessage())) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS001);
            }
            whatsappManager.send(request);
        } catch (Exception ex) {
            log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED, ex.getMessage(), ex);
        }
    }
}