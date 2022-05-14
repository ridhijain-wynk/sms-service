package in.wynk.sms.core.service;

import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.enums.SmsErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageScrubEngineImpl implements IScrubEngine {

    private final IMessageTemplateService templateService;

    @Override
    public void scrub(String message) {
       final MessageTemplateDTO templateDTO = templateService.findMessageTemplateFromSmsText(message);
       if (Objects.isNull(templateDTO)) throw new WynkRuntimeException(SmsErrorType.IQSMS001, "Template is not register, Hence Scrubbing message");
    }

}
