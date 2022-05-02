package in.wynk.sms.dto;

import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.queue.message.HighestPriorityMessage;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;

public class SMSFactory {

    public static SmsRequest getSmsRequest(SmsNotificationMessage message) {
        switch (message.getPriority()) {
            case HIGH:
                return HighPriorityMessage.from(message);
            case MEDIUM:
                return MediumPriorityMessage.from(message);
            case LOW:
                return LowPriorityMessage.from(message);
            default:
                throw new WynkRuntimeException("Unknown priority for message: " + message.getMessage() + " for msisdn: " + message.getMsisdn());
        }
    }

    public static SmsRequest getSmsRequest(SendSmsRequest request) {
        SMSPriority priority = SMSPriority.fromString(request.getPriority());
        switch (priority) {
            case HIGHEST:
                return HighestPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).build();
            case HIGH:
                return HighPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).build();
            case MEDIUM:
                return MediumPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).build();
            case LOW:
                return LowPriorityMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).build();
        }
        throw new IllegalArgumentException("Invalid message");
    }
}
