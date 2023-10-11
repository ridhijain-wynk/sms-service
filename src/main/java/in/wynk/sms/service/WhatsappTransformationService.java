package in.wynk.sms.service;

import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.common.dto.wa.outbound.common.Message;
import in.wynk.sms.common.dto.wa.outbound.constant.MessageType;
import in.wynk.sms.common.dto.wa.outbound.session.TextSessionMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.WhatsappRequestWrapper;
import in.wynk.sms.dto.request.WhatsappRequest;
import in.wynk.sms.enums.SmsErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static in.wynk.sms.common.constant.SMSPriority.HIGHEST;
import static in.wynk.sms.common.dto.wa.outbound.constant.MessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
    private final Map<MessageType, IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>> delegate = new HashMap<>();

    @PostConstruct
    public void init() {
        delegate.put(TEXT, new TextTransformationService());
    }
    @Override
    public AbstractWhatsappOutboundMessage transform (WhatsappRequestWrapper request) {
        return delegate.get(MessageType.fromString(request.getRequest().getMessageType())).transform(request);
    }

    private class TextTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
        @Override
        public TextSessionMessage transform (WhatsappRequestWrapper wrapper) {
            return TextSessionMessage.builder()
                    .sessionId(wrapper.getRequest().getSessionId())
                    .to(wrapper.getRequest().getMsisdn())
                    .from(wrapper.getWABANumber())
                    .type(MessageType.TEXT.getType())
                    .message(Message.builder().text(wrapper.getRequest().getMessage()).build()).build();
        }
    }
}