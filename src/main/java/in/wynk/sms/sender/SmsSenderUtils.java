package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.dto.request.SmsRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.*;
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
            if (Objects.nonNull(client)) {
                if (StringUtils.isNotEmpty(client.getMessageStrategy()) && MESSAGE_STRATEGY_IQ.equals(client.getMessageStrategy()) && !SMSPriority.HIGH.equals(request.getPriority())) {
                    smsSender = BeanLocatorFactory.getBean(AIRTEL_IQ_SMS_SENDER_BEAN, AbstractSMSSender.class);
                }
            }
        } catch(Exception ex) {
            logger.error(SMS_SEND_BEAN_ERROR,"error while initializing message bean for msisdn - " + request.getMsisdn(),ex);
            throw ex;
        }
        return smsSender;
    }
}
