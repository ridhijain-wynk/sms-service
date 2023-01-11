package in.wynk.sms.dto;

import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.sender.IMessageSender;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class MessageDetails {
    private SmsRequest message;
    private Map<String, IMessageSender<SmsRequest>> senderMap;
}