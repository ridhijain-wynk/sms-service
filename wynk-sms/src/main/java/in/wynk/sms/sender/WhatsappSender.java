package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.dto.wa.outbound.WhatsappMessageRequest;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.stream.producer.IKafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.PINPOINT_SENDER_BEAN;
import static in.wynk.sms.constants.SMSConstants.WHATSAPP_SENDER_BEAN;
import static in.wynk.sms.constants.SmsLoggingMarkers.SENDER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service(WHATSAPP_SENDER_BEAN)
public class WhatsappSender extends AbstractSMSSender {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SendersCachingService sendersCachingService;
    private final IKafkaEventPublisher<String, WhatsappMessageRequest> kafkaEventPublisher;

    @Override
    @AnalyseTransaction(name = "sendSmsWhatsapp")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @Override
    public void send(SmsRequest request) throws Exception {
        AnalyticService.update(request);
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }
        if(Objects.nonNull(client)){
            final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
            final Senders senders = sendersCachingService.getSenderByNameClientCountry(PINPOINT_SENDER_BEAN, client.getAlias(), request.getPriority(), countryCode);
            if(Objects.isNull(senders)){
                log.error(SENDER_NOT_FOUND, "Whatsapp sender not configured for client: {}, country : {}", client.getAlias(), countryCode);
                throw new WynkRuntimeException(SmsErrorType.SMS001);
            }
            if(StringUtils.equalsIgnoreCase(countryCode, BaseConstants.DEFAULT_COUNTRY_CODE)){
                //sendMessageDomestic(request, senders);
            } else {
                //sendMessageInternational(request, senders);
            }
        }
    }
}
