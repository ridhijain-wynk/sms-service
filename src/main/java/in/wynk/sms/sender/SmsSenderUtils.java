package in.wynk.sms.sender;

import in.wynk.advice.TimeIt;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.core.entity.SenderConfigurations;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.MessageServiceV2;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.dto.request.SmsRequest;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private SendersCachingService sendersCachingService;

    @Autowired
    private MessageServiceV2 messageServiceV2;

    @Override
    @TimeIt
    public Map<String, IMessageSender<SmsRequest>> fetchSmsSender(SmsRequest request) {
        Map<String, IMessageSender<SmsRequest>> senderMap = new HashMap<>();
        addSender(senderMap, PRIMARY, LOBBY_MESSAGE_STRATEGY);
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.nonNull(client)){
                final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
                SenderConfigurations senderConfigurations = senderConfigCachingService.getSenderConfigurationsByAliasAndCountry(client.getAlias(), countryCode);
                if(Objects.nonNull(senderConfigurations)){
                    Map<CommunicationType, SenderDetails> senderDetailsMap = senderConfigurations.getDetails().get(request.getPriority());
                    if(!CollectionUtils.isEmpty(senderDetailsMap) && senderDetailsMap.containsKey(request.getCommunicationType()) && senderDetailsMap.get(request.getCommunicationType()).isPrimaryPresent()){
                        final String primarySenderId = senderDetailsMap.get(request.getCommunicationType()).getPrimary();
                        addSender(senderMap, PRIMARY, sendersCachingService.getSenderById(primarySenderId).getName());
                        if(senderDetailsMap.get(request.getCommunicationType()).isSecondaryPresent()){
                            final String secondarySenderId = senderDetailsMap.get(request.getCommunicationType()).getSecondary();
                            addSender(senderMap, SECONDARY, sendersCachingService.getSenderById(secondarySenderId).getName());
                        }
                    }
                }
            }
            MessageTemplateDTO template = messageServiceV2.findMessagesFromSmsText(request.getText());
            if (Objects.nonNull(template) && Objects.nonNull(template.getSender()) &&
                    !senderMap.get(PRIMARY).equals(BeanLocatorFactory.getBean(
                            sendersCachingService.getSenderById(template.getSender()).getName(),
                            new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {}))){
                senderMap.put(SECONDARY, senderMap.get(PRIMARY));
                addSender(senderMap, PRIMARY, sendersCachingService.getSenderById(template.getSender()).getName());
            }
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
