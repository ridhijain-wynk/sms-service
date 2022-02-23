package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.dto.request.SmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.AIRTEL_SMS_SENDER;
import static in.wynk.sms.constants.SmsLoggingMarkers.SMS_SEND_BEAN_ERROR;

@Component
public class SmsSenderUtils implements ISmsSenderUtils{
    protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;

    @Override
    public AbstractSMSSender fetchSmsSender(SmsRequest request) {
        AbstractSMSSender smsSender = BeanLocatorFactory.getBean(AIRTEL_SMS_SENDER, AbstractSMSSender.class);
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.nonNull(client) && client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_SENDER").isPresent()) {
                final String senderBeanName = client.<String>getMeta(request.getPriority().name() + "_PRIORITY_SMS_SENDER").get();
                smsSender = BeanLocatorFactory.getBean(senderBeanName, AbstractSMSSender.class);
            }
        } catch(Exception ex) {
            logger.error(SMS_SEND_BEAN_ERROR,"error while initializing message bean for msisdn - " + request.getMsisdn(),ex);
            throw ex;
        }
        return smsSender;
    }
}
