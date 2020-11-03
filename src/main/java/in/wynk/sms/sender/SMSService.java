package in.wynk.sms.sender;

import in.wynk.sms.constants.Country;
import in.wynk.sms.model.SMSDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("smsService")
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class.getCanonicalName());



    @Autowired
    private AirtelSMSSender airtelSMSSender;


    public void sendMessage(SMSDto sms) {
        logger.debug("Started executing sendMessage() for ID:" + sms.getId());
        if (StringUtils.isNotEmpty(sms.getCountryCode())) {
            Country country = Country.getCountryByCountryCode(sms.getCountryCode());
            if (country.equals(Country.SRILANKA)) {
                airtelSMSSender.sendSmsToSriLanka(sms);
            } else {
                airtelSMSSender.sendMessage(sms.getMsisdn(), sms.getShortCode(), sms.getMessage(), sms.getPriority(), sms.getId());
            }
        } else {
            airtelSMSSender.sendMessage(sms.getMsisdn(), sms.getShortCode(), sms.getMessage(), sms.getPriority(), sms.getId());
        }
    }
}