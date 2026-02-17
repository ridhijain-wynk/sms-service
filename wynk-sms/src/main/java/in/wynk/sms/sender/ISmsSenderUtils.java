package in.wynk.sms.sender;

import in.wynk.sms.dto.request.SmsRequest;
import java.util.Map;

public interface ISmsSenderUtils<R extends SmsRequest, T extends IMessageSender<R>> {
     Map<String, T> fetchSmsSender(SmsRequest request);
}
