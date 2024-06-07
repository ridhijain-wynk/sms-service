package in.wynk.sms.dto;

import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationGCPMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.pubsub.message.HighPriorityGCPMessage;
import in.wynk.sms.pubsub.message.HighestPriorityGCPMessage;
import in.wynk.sms.pubsub.message.LowPriorityGCPMessage;
import in.wynk.sms.pubsub.message.MediumPriorityGCPMessage;
import in.wynk.sms.utils.BackwardServiceCompatibilitySupport;

public class SMSFactory {

    public static SmsRequest getSmsRequest(SmsNotificationGCPMessage message) {
        switch (message.getPriority()) {
            case HIGHEST:
                return HighestPriorityGCPMessage.from(message);
            case HIGH:
                return HighPriorityGCPMessage.from(message);
            case MEDIUM:
                return MediumPriorityGCPMessage.from(message);
            case LOW:
                return LowPriorityGCPMessage.from(message);
            default:
                throw new WynkRuntimeException("Unknown priority for message: " + message.getMessage() + " for msisdn: " + message.getMsisdn());
        }
    }

    public static SmsRequest getSmsRequest(SendSmsRequest request) {
        SMSPriority priority = SMSPriority.fromString(request.getPriority());
        final String alias = BackwardServiceCompatibilitySupport.resolve(request.getService(), request.getService());
        switch (priority) {
            case HIGHEST:
                return HighestPriorityGCPMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).clientAlias(alias).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).retryCount(request.getRetryCount()).build();
            case HIGH:
                return HighPriorityGCPMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).clientAlias(alias).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).retryCount(request.getRetryCount()).build();
            case MEDIUM:
                return MediumPriorityGCPMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).clientAlias(alias).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).retryCount(request.getRetryCount()).build();
            case LOW:
                return LowPriorityGCPMessage.builder().countryCode(request.getCountryCode())
                        .msisdn(request.getMsisdn()).clientAlias(alias).service(request.getService()).text(request.getMessage())
                        .messageId(request.getMsisdn() + System.currentTimeMillis()).retryCount(request.getRetryCount()).build();
        }
        throw new IllegalArgumentException("Invalid message");
    }

}
