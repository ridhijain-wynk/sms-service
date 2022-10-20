package in.wynk.sms.sender;

import in.wynk.sms.dto.request.SmsRequest;

public interface ISmsSenderUtils<R extends SmsRequest, T extends IMessageSender<R>> {
     T fetchSmsSender(SmsRequest request);
}
