package in.wynk.sms.test;

import in.wynk.sms.common.constant.SMSPriority;
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
                .text("NEW MOVIES FOR YOU! We've recently added 100+ movies & TV shows on your Airtel Xstream app in 10+ languages. Binge watch on bit.ly/2OjTyov")
                .build();
    }

    public static SmsRequest highPrioritySms2(String msisdn, String service) {
        return MediumPriorityMessage.builder()
                .messageId(msisdn + System.currentTimeMillis())
                .service(service)
                .msisdn(msisdn)
                .text("Kabira HELLOTUNE is about to expire on your mobile number in %s days. Extend validity for FREE on Wynk app wynk.onelink.me/3330602766/hello")
                .build();
    }
}
