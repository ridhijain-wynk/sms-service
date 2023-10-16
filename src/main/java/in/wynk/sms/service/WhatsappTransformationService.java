package in.wynk.sms.service;

import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.common.dto.wa.outbound.common.Message;
import in.wynk.sms.common.dto.wa.outbound.constant.MessageType;
import in.wynk.sms.common.dto.wa.outbound.session.ListSessionMessage;
import in.wynk.sms.common.dto.wa.outbound.session.MediaSessionMessage;
import in.wynk.sms.common.dto.wa.outbound.session.TextSessionMessage;
import in.wynk.sms.dto.WhatsappRequestWrapper;
import in.wynk.sms.dto.request.WhatsappRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static in.wynk.sms.common.dto.wa.outbound.constant.MessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
    private final Map<MessageType, IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>> delegate = new HashMap<>();

    @PostConstruct
    public void init() {
        delegate.put(TEXT, new TextTransformationService());
        delegate.put(MEDIA, new MediaTransformationService());
        delegate.put(LIST, new ListTransformationService());
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
                    .to(wrapper.getRequest().getTo())
                    .from(wrapper.getWABANumber())
                    .type(MessageType.TEXT.getType())
                    .message(Message.builder().text(wrapper.getRequest().getText()).build()).build();
        }
    }

    private class MediaTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
        @Override
        public MediaSessionMessage transform (WhatsappRequestWrapper wrapper) {
            final WhatsappRequest request = wrapper.getRequest();
            return MediaSessionMessage.builder()
                    .sessionId(request.getSessionId())
                    .to(request.getTo())
                    .from(wrapper.getWABANumber())
                    .type(LIST.getType())
                    .mediaAttachment(request.getMediaAttachment())
                    .build();
        }
    }

    private class ListTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
        @Override
        public ListSessionMessage transform (WhatsappRequestWrapper wrapper) {
            final WhatsappRequest request = wrapper.getRequest();
            return ListSessionMessage.builder()
                    .sessionId(request.getSessionId())
                    .to(request.getTo())
                    .from(wrapper.getWABANumber())
                    .type(LIST.getType())
                    .message(Message.builder()
                            .text(request.getText())
                            .build())
                    .list(request.getList())
                    .build();
        }
    }
}