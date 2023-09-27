package in.wynk.sms.kafka;

import in.wynk.sms.dto.request.whatsapp.WhatsappMessageRequest;

public interface IWhatsappSenderHandler<R,T> {
    R send(T t);
}