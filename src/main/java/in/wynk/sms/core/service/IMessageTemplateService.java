package in.wynk.sms.core.service;

import in.wynk.sms.dto.MessageTemplateDTO;

public interface IMessageTemplateService {
    MessageTemplateDTO findMessageTemplateFromSmsText(String message);
}
