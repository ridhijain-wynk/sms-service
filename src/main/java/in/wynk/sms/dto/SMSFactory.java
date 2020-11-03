package in.wynk.sms.dto;

import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.constants.SMSSource;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;
import org.springframework.stereotype.Component;

@Component
public class SMSFactory {

    public SmsRequest getSMSDto(SendSmsRequest request) {
        SMSPriority priority = SMSPriority.fromString(request.getPriority());
        switch (priority) {
            case HIGH:
                return HighPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis())
                        .shortCode(SMSSource.getShortCode(request.getService(), SMSPriority.HIGH)).build();
            case MEDIUM:
                return MediumPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis())
                        .shortCode(SMSSource.getShortCode(request.getService(), SMSPriority.MEDIUM)).build();

            case LOW:
                return LowPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis())
                        .shortCode(SMSSource.getShortCode(request.getService(), SMSPriority.LOW)).build();
        }
        throw new IllegalArgumentException("Invalid message");
    }

}
