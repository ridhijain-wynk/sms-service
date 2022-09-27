package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.entity.SenderConfigurations;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static in.wynk.sms.constants.SMSConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;

@Slf4j
@Component
public class SmsSenderUtils implements ISmsSenderUtils {
    protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;

    @Autowired
    private SenderConfigurationsCachingService senderConfigCachingService;

    @Autowired
    private SendersCachingService sendersCachingService;

    @Autowired
    private MessageCachingService messageCachingService;

    @Override
    public Map<String, IMessageSender<SmsRequest>> fetchSmsSender(SmsRequest request) {
        Map<String, IMessageSender<SmsRequest>> senderMap = new HashMap<>();
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put(PRIMARY, LOBBY_MESSAGE_STRATEGY);
        //addSender(senderMap, PRIMARY, LOBBY_MESSAGE_STRATEGY);
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.nonNull(client)){
                SenderConfigurations senderConfigurations = senderConfigCachingService.getSenderConfigurationsByAlias(client.getAlias());
                if(Objects.nonNull(senderConfigurations)){
                    Map<CommunicationType, SenderDetails> senderDetailsMap = senderConfigurations.getDetails().get(request.getPriority());
                    if(!CollectionUtils.isEmpty(senderDetailsMap) && senderDetailsMap.containsKey(request.getCommunicationType()) && senderDetailsMap.get(request.getCommunicationType()).isPrimaryPresent()){
                        final String primarySenderId = senderDetailsMap.get(request.getCommunicationType()).getPrimary();
                        //addSender(senderMap, PRIMARY, sendersCachingService.getSenderById(primarySenderId).getName());
                        tempMap.put(PRIMARY, sendersCachingService.getSenderById(primarySenderId).getName());
                        if(senderDetailsMap.get(request.getCommunicationType()).isSecondaryPresent()){
                            final String secondarySenderId = senderDetailsMap.get(request.getCommunicationType()).getSecondary();
                            //addSender(senderMap, SECONDARY, sendersCachingService.getSenderById(secondarySenderId).getName());
                            tempMap.put(SECONDARY, sendersCachingService.getSenderById(secondarySenderId).getName());
                        }
                    }
                }
            }
            log.error(SMS_SEND_BEAN_ERROR, "Client level sender fetched for request for "+ request.getMessageId()+ "- " + request.getMsisdn());
            if(Objects.nonNull(request.getTemplateId())){
                Messages message = messageCachingService.get(request.getTemplateId());
                if (Objects.nonNull(message) && Objects.nonNull(message.getSender()) &&
                        !tempMap.get(PRIMARY).equalsIgnoreCase(sendersCachingService.getSenderById(message.getSender()).getName())){
                    //senderMap.put(SECONDARY, senderMap.get(PRIMARY));
                    //addSender(senderMap, PRIMARY, sendersCachingService.getSenderById(message.getSender()).getName());
                    tempMap.put(SECONDARY, tempMap.get(PRIMARY));
                    tempMap.put(PRIMARY, sendersCachingService.getSenderById(message.getSender()).getName());
                }
            }
            tempMap.forEach((k,v)-> addSender(senderMap, k, v));
            log.error(SMS_SEND_BEAN_ERROR, "Template level sender fetched for request for "+ request.getMessageId()+ "- " + request.getMsisdn());
        } catch (Exception ex) {
            logger.error(SMS_SEND_BEAN_ERROR, "error while initializing message bean for msisdn - " + request.getMsisdn(), ex);
            throw ex;
        }
        return senderMap;
    }

    private void addSender(Map<String, IMessageSender<SmsRequest>> senderMap, String beanType, String beanName) {
        try {
            senderMap.put(beanType, BeanLocatorFactory.getBean(beanName, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
            }));
        } catch(Exception e){
            logger.error(SMS_SEND_BEAN_ERROR, "error while adding " + beanName + " bean for - " + beanType);
        }
    }
}
