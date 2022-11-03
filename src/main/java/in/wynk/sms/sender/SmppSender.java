package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.smpp.core.dto.Message;
import in.wynk.smpp.core.dto.MessageResponse;
import in.wynk.smpp.core.sender.manager.SenderManager;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.utils.SMSUtils;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static in.wynk.sms.constants.SmsLoggingMarkers.SENDER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service(SMSBeanConstant.SMPP_SENDER_WRAPPER)
public class SmppSender extends AbstractSMSSender {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SendersCachingService sendersCachingService;

    @Override
    @AnalyseTransaction(name = "sendSmsSmsc")
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
        final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
        Senders senders = sendersCachingService.getSenderByNameClientCountry(SMSBeanConstant.SMPP_SENDER_WRAPPER, client.getAlias(), request.getPriority(), countryCode);
        if(Objects.isNull(senders)){
            log.error(SENDER_NOT_FOUND, "SMPP sender not configured for client: {}, country : {}", client.getAlias(), countryCode);
            throw new WynkRuntimeException(SmsErrorType.SMS001);
        }
        String shortCode = senders.getShortCode();
        shortCode = SMSUtils.getShortCode(request.getTemplateId(), request.getPriority(), client.getAlias(), shortCode, countryCode);
        final SenderManager senderManager = BeanLocatorFactory.getBean(client.getAlias() + SMSBeanConstant.SMPP_SENDER_MANAGER_BEAN, SenderManager.class);
        final MessageResponse response = senderManager.send(Message.simple(request.getText()).messageId(request.getMessageId()).from(shortCode).to(request.getMsisdn()).build());
        AnalyticService.update(response);
    }

}
