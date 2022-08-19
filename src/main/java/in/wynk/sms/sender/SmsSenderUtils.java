package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.dto.request.SmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.*;

import static in.wynk.sms.constants.SMSConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;

@Component
public class SmsSenderUtils implements ISmsSenderUtils {
    protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;

    @Override
    public Map<String, IMessageSender<SmsRequest>> fetchSmsSender(SmsRequest request) {
        Map<String, IMessageSender<SmsRequest>> senderMap = new HashMap<>();
        IMessageSender<SmsRequest> smsSender = BeanLocatorFactory.getBean(LOBBY_MESSAGE_STRATEGY, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
        });
        senderMap.put(PRIMARY, smsSender);
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.nonNull(client) && client.getMeta(request.getPriority().name() + "_PRIORITY_" + request.getCommunicationType().name() + "_" + PRIMARY + "_SENDER").isPresent()) {
                final String primarySenderBeanName = client.<String>getMeta(request.getPriority().name() + "_PRIORITY_" + request.getCommunicationType().name() + "_" + PRIMARY + "_SENDER").get();
                senderMap.put(PRIMARY, BeanLocatorFactory.getBean(primarySenderBeanName, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
                }));
            }
            if (Objects.nonNull(client) && client.getMeta(request.getPriority().name() + "_PRIORITY_" + request.getCommunicationType().name() + "_" + SECONDARY + "_SENDER").isPresent()) {
                final String secondarySenderBeanName = client.<String>getMeta(request.getPriority().name() + "_PRIORITY_" + request.getCommunicationType().name() + "_" + SECONDARY + "_SENDER").get();
                senderMap.put(SECONDARY, BeanLocatorFactory.getBean(secondarySenderBeanName, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
                }));
            }
        } catch (Exception ex) {
            logger.error(SMS_SEND_BEAN_ERROR, "error while initializing message bean for msisdn - " + request.getMsisdn(), ex);
            throw ex;
        }
        return senderMap;
    }
}
