package in.wynk.sms.service;

import com.github.annotation.analytic.core.service.AnalyticService;
import com.google.gson.Gson;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.dto.wa.outbound.WhatsappMessageRequest;
import in.wynk.sms.common.dto.wa.outbound.constant.MessageType;
import in.wynk.sms.common.dto.wa.outbound.session.*;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateMultiRecipientMessage;
import in.wynk.sms.common.dto.wa.outbound.template.BulkTemplateSingleRecipientMessage;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.dto.response.WhatsappMessageResponse;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.event.WhatsappOrderDetailsEvent;
import in.wynk.sms.kafka.IWhatsappSenderHandler;
import in.wynk.sms.utils.WhatsappUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static in.wynk.common.constant.BaseConstants.HTTP_STATUS_CODE;
import static in.wynk.sms.common.dto.wa.outbound.constant.MessageType.*;

@Slf4j
@Service(SMSBeanConstant.WHATSAPP_MANAGER)
@RequiredArgsConstructor
public class WhatsappManagerService implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {

    @Autowired
    private Gson gson;
    @Autowired
    private Map<String, RestTemplate> clientRestTemplates;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("#{${iq.whatsapp.endpoints}}")
    private Map<String, String> endpoints;
    @Value("#{${iq.whatsapp.credentials}}")
    private Map<String, String> credentials;
    private final Map<MessageType, IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest>> delegate = new HashMap<>();

    @PostConstruct
    public void init() {
        delegate.put(TEXT, new TextMessageHandler());
        delegate.put(MEDIA, new MediaMessageHandler());
        delegate.put(BUTTON, new ButtonMessageHandler());
        delegate.put(LIST, new ListMessageHandler());
        delegate.put(LOCATION, new LocationMessageHandler());
        delegate.put(SINGLE_PRODUCT, new SingleProductMessageHandler());
        delegate.put(MULTI_PRODUCT, new MultiProductMessageHandler());
        delegate.put(CONTACTS, new ContactsMessageHandler());
        delegate.put(ORDER_DETAILS, new OrderDetailsMessageHandler());
        delegate.put(ORDER_STATUS, new OrderStatusMessageHandler());
        delegate.put(SINGLE_TEMPLATE, new SingleTemplateMessageHandler());
        delegate.put(MULTI_TEMPLATE, new SingleTemplateMessageHandler());
        delegate.put(BULK_SINGLE_TEMPLATE, new BulkTemplateSingleRecipientMessageHandler());
        delegate.put(BULK_MULTI_TEMPLATE, new BulkTemplateMultiRecipientMessageHandler());
    }

    @Override
    public WhatsappMessageResponse send (WhatsappMessageRequest request) {
        if(!clientRestTemplates.containsKey(request.getClientAlias())){
            throw new WynkRuntimeException(SmsErrorType.WHSMS003);
        }
        return delegate.get(request.getMessage().getMessageType()).send(request);
    }

    private class TextMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            final TextSessionMessage message = (TextSessionMessage) request.getMessage();
            final String url = endpoints.get(TEXT.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class MediaMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final MediaSessionMessage message = (MediaSessionMessage) request.getMessage();
            final String url = endpoints.get(MEDIA.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class ButtonMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final ButtonSessionMessage message = (ButtonSessionMessage) request.getMessage();
            final String url = endpoints.get(BUTTON.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class ListMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final ListSessionMessage message = (ListSessionMessage) request.getMessage();
            final String url = endpoints.get(LIST.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class LocationMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final LocationSessionMessage message = (LocationSessionMessage) request.getMessage();
            final String url = endpoints.get(LOCATION.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class SingleProductMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final SingleProductSessionMessage message = (SingleProductSessionMessage) request.getMessage();
            final String url = endpoints.get(SINGLE_PRODUCT.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class MultiProductMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final MultiProductSessionMessage message = (MultiProductSessionMessage) request.getMessage();
            final String url = endpoints.get(MULTI_PRODUCT.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class ContactsMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final ContactsSessionMessage message = (ContactsSessionMessage) request.getMessage();
            final String url = endpoints.get(CONTACTS.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class OrderDetailsMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final OrderDetailsSessionMessage message = (OrderDetailsSessionMessage) request.getMessage();
            final String url = endpoints.get(ORDER_DETAILS.getType());
            final WhatsappMessageResponse response = post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            eventPublisher.publishEvent(WhatsappOrderDetailsEvent.builder().message(message).requestId(request.getRequestId()).orgId(request.getOrgId()).serviceId(request.getServiceId()).sessionId(message.getSessionId()).response(response).build());
            return response;
        }
    }

    private class OrderStatusMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            final OrderStatusSessionMessage message = (OrderStatusSessionMessage) request.getMessage();
            final String url = endpoints.get(ORDER_STATUS.getType());
            return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
        }
    }

    private class SingleTemplateMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            final String url = endpoints.get("TEMPLATE");
            return post(url, request.getClientAlias(), request.getMessage(), WhatsappMessageResponse.class);
        }
    }

    private class BulkTemplateSingleRecipientMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            final BulkTemplateSingleRecipientMessage message = (BulkTemplateSingleRecipientMessage) request.getMessage();
            final String url = endpoints.get("BULK_TEMPLATE");
            return post(url, request.getClientAlias(), message.getData(), WhatsappMessageResponse.class);
        }
    }

    private class BulkTemplateMultiRecipientMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            final BulkTemplateMultiRecipientMessage message = (BulkTemplateMultiRecipientMessage) request.getMessage();
            final String url = endpoints.get("BULK_TEMPLATE");
            return post(url, request.getClientAlias(), message.getData(), WhatsappMessageResponse.class);
        }
    }

    @SneakyThrows
    public <T> T post(String url, String service, Object requestBody, Class<T> clazz) {
        long currentTime = System.currentTimeMillis();
        final URI uri = new URI(url);
        AnalyticService.update("url", uri.toString());
        final HttpHeaders headers = WhatsappUtils.getBasicAuthHeaders(credentials.get("username"), credentials.get("password"));
        final HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
        AnalyticService.update("request", gson.toJson(requestBody));
        final ResponseEntity<String> responseEntity = clientRestTemplates.get(service).exchange(uri, HttpMethod.POST, entity, String.class);
        final T response = gson.fromJson(responseEntity.getBody(), clazz);
        AnalyticService.update(HTTP_STATUS_CODE, responseEntity.getStatusCode().name());
        AnalyticService.update("timeTaken", System.currentTimeMillis() - currentTime);
        return response;
    }
}
