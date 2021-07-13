package in.wynk.sms.sender;

import in.wynk.sms.dto.request.SmsRequest;

public interface ISmsSenderUtils {
    AbstractSMSSender fetchSmsSender(SmsRequest request);
}
