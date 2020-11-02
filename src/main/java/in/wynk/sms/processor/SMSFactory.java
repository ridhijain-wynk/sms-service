package in.wynk.sms.processor;

import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SMSFactory {

    private static final Logger logger = LoggerFactory.getLogger(SMSFactory.class);

    public SMSDto getSMSDto(SendSmsRequest request) {

        try {
            SMSDto SMSDto;
            Integer retryCount = 0;
            if (request.getRetryCount() != null) {
                retryCount = request.getRetryCount();
            }
            if (request.getPriority().equalsIgnoreCase(SMSPriority.HIGH.name())) {
                SMSDto = new HighPrioritySmsDto(request.getMessage(), request.getMsisdn(), request.getUseDnd(), request.getSource(), request.isNineToNine(), retryCount);

            } else if (request.getPriority().equalsIgnoreCase(SMSPriority.MEDIUM.name())) {
                SMSDto = new MediumPrioritySmsDto(request.getMessage(), request.getMsisdn(), request.getUseDnd(), request.getSource(), request.isNineToNine(), retryCount);

            } else {
                SMSDto = new LowPrioritySmsDto(request.getMessage(), request.getMsisdn(), request.getUseDnd(), request.getSource(), request.isNineToNine(), retryCount);
            }
            String service = StringUtils.isBlank(request.getService()) ? request.getSource() : request.getService();
            SMSDto.setService(service);
            return SMSDto;

        } catch (Exception e) {
            logger.error("Error in SMSFactory", e);
            return null;
        }

    }

}
