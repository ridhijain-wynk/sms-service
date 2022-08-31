package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.core.entity.SenderConfigurations;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.dto.request.SmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static in.wynk.sms.constants.SMSConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;

@Component
public class SmsSenderUtils implements ISmsSenderUtils {
    protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;

    @Autowired
    private SenderConfigurationsCachingService senderConfigCachingService;

    @Override
    public Map<String, IMessageSender<SmsRequest>> fetchSmsSender(SmsRequest request) {
        Map<String, IMessageSender<SmsRequest>> senderMap = new HashMap<>();
        senderMap.put(PRIMARY, BeanLocatorFactory.getBean(LOBBY_MESSAGE_STRATEGY, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
        }));
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.isNull(client)) return senderMap;
            SenderConfigurations senderConfigurations = senderConfigCachingService.getSenderConfigurationsByAlias(client.getAlias());
            if(Objects.nonNull(senderConfigurations)){
                Map<CommunicationType, SenderDetails> senderDetailsMap = senderConfigurations.getDetails().get(request.getPriority());
                if(!CollectionUtils.isEmpty(senderDetailsMap) && senderDetailsMap.containsKey(request.getCommunicationType()) && senderDetailsMap.get(request.getCommunicationType()).isPrimaryPresent()){
                    final String primarySenderBeanName = senderDetailsMap.get(request.getCommunicationType()).getPrimary();
                    senderMap.put(PRIMARY, BeanLocatorFactory.getBean(primarySenderBeanName, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
                    }));
                    if(senderDetailsMap.get(request.getCommunicationType()).isSecondaryPresent()){
                        final String secondarySenderBeanName = senderDetailsMap.get(request.getCommunicationType()).getSecondary();
                        senderMap.put(SECONDARY, BeanLocatorFactory.getBean(secondarySenderBeanName, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
                        }));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(SMS_SEND_BEAN_ERROR, "error while initializing message bean for msisdn - " + request.getMsisdn(), ex);
            throw ex;
        }
        return senderMap;
    }
}
