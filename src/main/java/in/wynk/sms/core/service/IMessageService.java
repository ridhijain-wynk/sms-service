package in.wynk.sms.core.service;

import in.wynk.sms.dto.MessageTemplateDTO;

public interface IMessageService {
    MessageTemplateDTO findMessagesFromSmsText(String message);
}
