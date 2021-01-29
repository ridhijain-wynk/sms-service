package in.wynk.sms.test;

import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;

public class SmsTestUtils {

    public static SmsRequest lowPrioritySms(String msisdn, String service, String clientAlias) {
        return LowPriorityMessage.builder()
                .messageId(msisdn + System.currentTimeMillis())
                .service(service).clientAlias(clientAlias)
                .msisdn(msisdn)
                .text("Low priority SMS नमस्कार <&> https://www.google.com")
                .build();
    }

    public static SmsRequest lowPrioritySms(String msisdn, String service) {
        return LowPriorityMessage.builder()
                .messageId(msisdn + System.currentTimeMillis())
                .service(service)
                .msisdn(msisdn)
                .text("Low priority SMS https://www.google.com")
                .build();
    }

    public static SmsRequest mediumPrioritySms(String msisdn, String service) {
        return MediumPriorityMessage.builder()
                .messageId(msisdn + System.currentTimeMillis())
                .service(service)
                .msisdn(msisdn)
                .text("Medium priority SMS https://www.yahoo.com")
                .build();
    }

    public static SmsRequest highPrioritySms(String msisdn, String service) {
        return MediumPriorityMessage.builder()
                .messageId(msisdn + System.currentTimeMillis())
                .service(service)
                .msisdn(msisdn)
                .text("High priority SMS https://www.apple.com")
                .build();
    }
}
