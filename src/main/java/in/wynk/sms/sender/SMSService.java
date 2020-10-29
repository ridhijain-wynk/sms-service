package in.wynk.sms.sender;

import in.wynk.sms.constants.Country;
import in.wynk.sms.model.SMSDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service("smsService")
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class.getCanonicalName());

    private AbstractSMSSender smsSender;

    @Autowired
    private AirtelSMSSender airtelSMSSender;

    @Autowired
    DummyTask dummy;

    @PostConstruct
    private void init() {
        smsSender = airtelSMSSender;
    }

    @PreDestroy
    private void destroy() {
        if (null != smsSender) {
            smsSender.shutdown();
        }
    }

    @Value("${environment:dev1}")
    private String environment;

    //    /**
//     *
//     * @param msisdn : Phone number
//     * @param fromShortCode : Short code for sms
//     * @param message : Message for SMS
//     * @param createTimestamp : Time at the which SMS request came
//     * @param useDND
//     */
    public void sendMessage(SMSDto sms) {
        logger.debug("Started executing sendMessage() for ID:" + sms.getId());

        if (environment.equalsIgnoreCase("prod")) {
            if (StringUtils.isNotEmpty(sms.getCountryCode())) {
                Country country = Country.getCountryByCountryCode(sms.getCountryCode());
                if (country.equals(Country.SRILANKA)) {
                    airtelSMSSender.sendSmsToSriLanka(sms);
                } else {
                    airtelSMSSender.sendMessage(sms.getMsisdn(), sms.getShortCode(), sms.getMessage(), sms.isUseDnd(), sms.getCreationTimestamp(), sms.getPriority(), sms.getId());
                }
            } else {
                airtelSMSSender.sendMessage(sms.getMsisdn(), sms.getShortCode(), sms.getMessage(), sms.isUseDnd(), sms.getCreationTimestamp(), sms.getPriority(), sms.getId());
            }
        } else {
            dummy.dummyCall(sms.getMsisdn(), sms.getShortCode(), sms.getMessage(), sms.isUseDnd(), sms.getCreationTimestamp(), sms.getPriority(), sms.getId());
        }

    }


    public String getConnectionPoolStats() {
        String response = StringUtils.EMPTY;
        if (null != smsSender) {
            response = smsSender.getConnectionPoolStats();
        }
        return response;
    }

    public String getThreadPoolStats() {
        String response = StringUtils.EMPTY;
        if (null != smsSender) {
            response = smsSender.getThreadPoolStats();
        }
        return response;
    }

    public String getResponseCodeStats() {
        String response = StringUtils.EMPTY;
        if (null != smsSender) {
            response = smsSender.getResponseCodeStats();
        }
        return response;
    }
}