package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.constants.SMSConstants;
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

    private final IMessageService templateService;

    @Override
    public void scrub(String message) {

        final MessageTemplateDTO templateDTO = templateService.findMessagesFromSmsText(message);
        if (Objects.isNull(templateDTO)) {
            AnalyticService.update(SMSConstants.IS_MESSAGE_SCRUBBED, true);
            throw new WynkRuntimeException(SmsErrorType.IQSMS001, "Template is not register, Hence Scrubbing message");
        }
        AnalyticService.update(SMSConstants.IS_MESSAGE_SCRUBBED, false);
        AnalyticService.update(SMSConstants.SCRUBBING_TEMPLATE_ID, templateDTO.getMessageTemplateId());

    }

}
