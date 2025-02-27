package in.wynk.sms.service;

import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.common.dto.wa.outbound.common.*;
import in.wynk.sms.common.dto.wa.outbound.constant.MessageType;
import in.wynk.sms.common.dto.wa.outbound.session.ListSessionMessage;
import in.wynk.sms.common.dto.wa.outbound.session.MediaSessionMessage;
import in.wynk.sms.common.dto.wa.outbound.session.TextSessionMessage;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateMultiRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateSingleRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.SingleTemplateMultiRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.SingleTemplateSingleRecipientMessage;
import in.wynk.sms.dto.WhatsappRequestWrapper;
import in.wynk.sms.dto.request.WhatsappRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.*;

import static in.wynk.sms.common.dto.wa.outbound.constant.MessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper> {
    private final Map<String, IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>> delegator = new HashMap<>();
    @PostConstruct
    public void init() {
        delegator.put("session", new SessionBasedTransformationService());
        delegator.put("template", new TemplateBasedTransformationService());
    }
    @Override
    public AbstractWhatsappOutboundMessage transform (WhatsappRequestWrapper request) {
        if(!StringUtils.isEmpty(request.getRequest().getTemplateId())){
            return delegator.get("template").transform(request);
        }
        return delegator.get("session").transform(request);
    }

    private class SessionBasedTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
        private final Map<MessageType, IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>> delegate = new HashMap<>();
        public SessionBasedTransformationService() {
            delegate.put(TEXT, new TextTransformationService());
            delegate.put(MEDIA, new MediaTransformationService());
            delegate.put(LIST, new ListTransformationService());
        }

        @Override
        public AbstractWhatsappOutboundMessage transform (WhatsappRequestWrapper wrapper) {
            return delegate.get(MessageType.fromString(wrapper.getRequest().getMessageType())).transform(wrapper);
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
                        .mediaAttachment(wrapper.getRequest().getMediaAttachment())
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

    private class TemplateBasedTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
        private final Map<MessageType, IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>> delegate = new HashMap<>();
        public TemplateBasedTransformationService() {
            delegate.put(MULTI_TEMPLATE, new MultiTemplateTransformationService());
            delegate.put(SINGLE_TEMPLATE, new SingleTemplateTransformationService());
            delegate.put(BULK_SINGLE_TEMPLATE, new BulkSingleTemplateTransformationService());
            delegate.put(BULK_MULTI_TEMPLATE, new BulkMultiTemplateTransformationService());
        }
        @Override
        public AbstractWhatsappOutboundMessage transform (WhatsappRequestWrapper wrapper) {
            return delegate.get(MessageType.fromString(wrapper.getRequest().getMessageType())).transform(wrapper);
        }

        private class MultiTemplateTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
            @Override
            public SingleTemplateMultiRecipientMessage transform (WhatsappRequestWrapper wrapper) {
                return SingleTemplateMultiRecipientMessage.builder()
                        .from(wrapper.getWABANumber())
                        .recipients(Arrays.asList(wrapper.getRequest().getTo()))
                        .type(MULTI_TEMPLATE.getType())
                        .message(wrapper.getRequest().getMessageBody())
                        .mediaAttachment(wrapper.getRequest().getMedia())
                        .callBackUrls(wrapper.getRequest().getCallBackUrls())
                        .templateId(wrapper.getRequest().getTemplateId())
                        .build();
            }
        }

        private class SingleTemplateTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
            @Override
            public SingleTemplateSingleRecipientMessage transform (WhatsappRequestWrapper wrapper) {
                return SingleTemplateSingleRecipientMessage.builder()
                        .from(wrapper.getWABANumber())
                        .to(wrapper.getRequest().getTo())
                        .type(SINGLE_TEMPLATE.getType())
                        .message(wrapper.getRequest().getMessageBody())
                        .mediaAttachment(wrapper.getRequest().getMedia())
                        .callBackUrls(wrapper.getRequest().getCallBackUrls())
                        .templateId(wrapper.getRequest().getTemplateId())
                        .build();
            }
        }

        private class BulkSingleTemplateTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
            @Override
            public BulkTemplateSingleRecipientMessage transform (WhatsappRequestWrapper wrapper) {
                return BulkTemplateSingleRecipientMessage.builder()
                        .from(wrapper.getWABANumber())
                        .templateId(wrapper.getRequest().getTemplateId())
                        .data(Arrays.asList(SingleTemplateSingleRecipientMessage.builder().build()))
                        .type(BULK_SINGLE_TEMPLATE.getType())
                        .build();
            }
        }

        private class BulkMultiTemplateTransformationService implements IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper>{
            @Override
            public BulkTemplateMultiRecipientMessage transform (WhatsappRequestWrapper wrapper) {
                return BulkTemplateMultiRecipientMessage.builder()
                        .from(wrapper.getWABANumber())
                        .templateId(wrapper.getRequest().getTemplateId())
                        .data(Arrays.asList(SingleTemplateMultiRecipientMessage.builder().build()))
                        .type(BULK_MULTI_TEMPLATE.getType())
                        .build();
            }
        }
    }
}