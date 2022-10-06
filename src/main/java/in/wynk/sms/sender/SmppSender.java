package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.smpp.core.dto.Message;
import in.wynk.smpp.core.dto.MessageResponse;
import in.wynk.smpp.core.sender.manager.SenderManager;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.utils.SMSUtils;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
        String shortCode = sendersCachingService.getSenderByNameAndClient(SMSBeanConstant.SMPP_SENDER_WRAPPER, client.getAlias(), request.getPriority()).getShortCode();
        shortCode = SMSUtils.getShortCode(request.getTemplateId(), request.getPriority(), client.getAlias(), shortCode);
        final SenderManager senderManager = BeanLocatorFactory.getBean(client.getAlias() + SMSBeanConstant.SMPP_SENDER_MANAGER_BEAN, SenderManager.class);
        final MessageResponse response = senderManager.send(Message.simple(request.getText()).messageId(request.getMessageId()).from(shortCode).to(request.getMsisdn()).build());
        AnalyticService.update(response);
    }

}
