package in.wynk.sms.test;

import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.queue.message.HighPriorityMessage;
import in.wynk.sms.queue.message.LowPriorityMessage;
import in.wynk.sms.queue.message.MediumPriorityMessage;

public class SmsTestUtils {

    public static SmsRequest lowPrioritySms(String msisdn) {
        return LowPriorityMessage.from(SmsNotificationMessage.builder().message("Low priority SMS https://www.google.com").priority(SMSPriority.LOW.getSmsPriority()).service("WYNK").msisdn(msisdn).build());
    }

    public static SmsRequest mediumPrioritySms(String msisdn) {
        return MediumPriorityMessage.from(SmsNotificationMessage.builder().message("Medium priority SMS https://www.yahoo.com").priority(SMSPriority.LOW.getSmsPriority()).service("WYNK").msisdn(msisdn).build());
    }

    public static SmsRequest highPrioritySms(String msisdn) {
        return HighPriorityMessage.from(SmsNotificationMessage.builder().message("High priority SMS https://www.apple.com").priority(SMSPriority.LOW.getSmsPriority()).service("WYNK").msisdn(msisdn).build());
    }
}
