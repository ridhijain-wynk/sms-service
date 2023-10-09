package in.wynk.sms.kafka;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.core.constant.ClientErrorType;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.dto.wa.outbound.WhatsappMessageRequest;
import in.wynk.sms.constants.SmsLoggingMarkers;
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
    private final ClientDetailsCachingService clientDetailsCachingService;

    public WhatsappKafkaConsumptionHandler (WhatsappManagerService whatsappManager, ClientDetailsCachingService clientDetailsCachingService) {
        this.whatsappManager = whatsappManager;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @Override
    public void sendMessage(WhatsappMessageRequest request) {
        try {
            AnalyticService.update(request);
            if (ObjectUtils.isEmpty(request) || Objects.isNull(request.getService()) || Objects.isNull(request.getMessage()) ||
                    Objects.isNull(request.getMessage().getMessageType())) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS001);
            }
            final Client client = clientDetailsCachingService.getClientByService(request.getService());
            if(Objects.isNull(client)){
                throw new WynkRuntimeException(ClientErrorType.CLIENT001);
            }
            whatsappManager.send(request);
        } catch (WynkRuntimeException ex) {
            log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED_NO_RETRY, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED_RETRY, ex.getMessage(), ex);
            throw ex;
        }
    }

    /*private boolean isValidRequest(WhatsappMessageRequest request){
        if (ObjectUtils.isEmpty(request) || Objects.isNull(request.getClientAlias()) || Objects.isNull(request.getMessage())) {
            throw new WynkRuntimeException(SmsErrorType.WHSMS001);
        }
        if (!ObjectUtils.isEmpty(request) && Objects.nonNull(request.getClientAlias()) && Objects.nonNull(request.getMessage())){
            final Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if(Objects.nonNull(client)){
                if(Objects.nonNull(request.getMessage().getType())){

                }
            } else {

            }
            return false;
        }
        return false;
    }*/
}