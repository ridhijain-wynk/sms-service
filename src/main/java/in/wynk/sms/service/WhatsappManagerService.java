package in.wynk.sms.service;

import com.google.gson.Gson;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.MessageType;
import in.wynk.sms.common.dto.whatsapp.*;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.common.dto.whatsapp.BulkTemplateOutboundMessage;
import in.wynk.sms.common.dto.whatsapp.SingleTemplateOutboundMessage;
import in.wynk.sms.dto.response.WhatsappMessageResponse;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.kafka.IWhatsappSenderHandler;
import in.wynk.sms.utils.WhatsappUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static in.wynk.sms.common.constant.MessageType.*;

@Slf4j
@Service(SMSBeanConstant.WHATSAPP_MANAGER)
@RequiredArgsConstructor
public class WhatsappManagerService implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {

    @Autowired
    private Gson gson;
    @Autowired
    private Map<String, RestTemplate> clientRestTemplates;
    @Value("${iq.whatsapp.session.url}")
    private String iqWhatsappUrl;
    @Value("#{${iq.whatsapp.session.endpoints}}")
    private Map<String, String> sessionEndpoints;
    @Value("#{${iq.whatsapp.template.endpoints}}")
    private Map<String, String> templateEndpoints;
    @Value("#{${iq.whatsapp.session.credentials}}")
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
        delegate.put(TEMPLATE, new SingleTemplateMessageHandler());
        delegate.put(BULK_TEMPLATE, new BulkTemplateMessageHandler());
    }

    @Override
    public WhatsappMessageResponse send (WhatsappMessageRequest request) {
        final AbstractWhatsappOutboundMessage message = (AbstractWhatsappOutboundMessage) request.getMessage();
        return delegate.get(message.getMessageType()).send(request);
    }

    private class TextMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            try {
                final TextSessionMessage message = (TextSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(TEXT.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class MediaMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final MediaSessionMessage message = (MediaSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(MEDIA.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class ButtonMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final ButtonSessionMessage message = (ButtonSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(BUTTON.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class ListMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final ListSessionMessage message = (ListSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(LIST.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class LocationMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final LocationSessionMessage message = (LocationSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(LOCATION.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class SingleProductMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final SingleProductSessionMessage message = (SingleProductSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(SINGLE_PRODUCT.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class MultiProductMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final MultiProductSessionMessage message = (MultiProductSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(MULTI_PRODUCT.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class ContactsMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final ContactsSessionMessage message = (ContactsSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(CONTACTS.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class OrderDetailsMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final OrderDetailsSessionMessage message = (OrderDetailsSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(ORDER_DETAILS.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class OrderStatusMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest request) {
            try {
                final OrderStatusSessionMessage message = (OrderStatusSessionMessage) request.getMessage();
                final String url = iqWhatsappUrl + sessionEndpoints.get(ORDER_STATUS.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class SingleTemplateMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            try {
                final SingleTemplateOutboundMessage message = (SingleTemplateOutboundMessage) request.getMessage();
                final String url = iqWhatsappUrl + templateEndpoints.get(TEMPLATE.getType());
                return post(url, request.getClientAlias(), message, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class BulkTemplateMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest request) {
            try {
                final BulkTemplateOutboundMessage message = (BulkTemplateOutboundMessage) request.getMessage();
                final String url = iqWhatsappUrl + templateEndpoints.get(BULK_TEMPLATE.getType());
                return post(url, request.getClientAlias(), message.getData(), WhatsappMessageResponse.class);
            } catch (Exception ex) {
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    public <T> T post(String url, String clientAlias, Object requestBody, Class<T> clazz) throws Exception {
        long currentTime = System.currentTimeMillis();
        final URI uri = new URI(url);
        final HttpHeaders headers = WhatsappUtils.getHMACAuthHeaders(credentials.get("username"), credentials.get("password"));
        final HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
        log.info("IQ Whatsapp Request url : [{}]", uri.getPath());
        String responseStr = clientRestTemplates.get(clientAlias).exchange(uri.toString(), HttpMethod.POST, entity, String.class).getBody();
        final T response = gson.fromJson(responseStr, clazz);
        log.info("Time taken : [{}]", System.currentTimeMillis() - currentTime);
        return response;
    }
}
