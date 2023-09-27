package in.wynk.sms.service;

import com.google.gson.Gson;
import in.wynk.auth.utils.EncryptUtils;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.http.constant.HttpConstant;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.dto.request.whatsapp.MediaMessageRequest;
import in.wynk.sms.dto.request.whatsapp.TextMessageRequest;
import in.wynk.sms.dto.request.whatsapp.WhatsappMessageRequest;
import in.wynk.sms.dto.request.whatsapp.WhatsappSendMessage;
import in.wynk.sms.dto.response.WhatsappMessageResponse;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.enums.WhatsappMessageType;
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
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service(SMSBeanConstant.WHATSAPP_MANAGER)
@RequiredArgsConstructor
public class WhatsappManagerService implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {

    @Autowired
    private Map<String, RestTemplate> clientRestTemplates;
    @Autowired
    private Gson gson;
    @Value("${iq.whatsapp.session.url}")
    private String iqWhatsappUrl;
    @Value("#{${iq.whatsapp.session.endpoints}}")
    private Map<String, String> endpoints;
    @Value("#{${iq.whatsapp.session.credentials}}")
    private Map<String, String> credentials;
    private final Map<Class<? extends WhatsappMessageRequest>, IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest>> delegate = new HashMap<>();

    @PostConstruct
    public void init() {
        delegate.put(TextMessageRequest.class, new TextMessageHandler());
        delegate.put(MediaMessageRequest.class, new MediaMessageHandler());
    }

    @Override
    public WhatsappMessageResponse send (WhatsappMessageRequest request) {
        return delegate.get(request.getClass()).send(request);
    }

    public <T> T post(String url, String clientAlias, WhatsappMessageRequest request, Class<T> clazz) throws Exception {
        long currentTime = System.currentTimeMillis();
        final URI uri = new URI(url);
        final HttpHeaders headers = WhatsappUtils.getHMACAuthHeaders(credentials.get("username"), credentials.get("password"));
        final HttpEntity<?> entity = new HttpEntity<>(request, headers);
        log.info("IQ Whatsapp Request url : [{}]", uri.getPath());
        String responseStr = clientRestTemplates.get(clientAlias).exchange(uri.toString(), HttpMethod.POST, entity, String.class).getBody();
        final T response = gson.fromJson(responseStr, clazz);
        log.info("Time taken : [{}]", System.currentTimeMillis() - currentTime);
        return response;
    }

    private class TextMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        public TextMessageHandler(){}

        @Override
        public WhatsappMessageResponse send(WhatsappMessageRequest t) {
            try {
                final TextMessageRequest request = (TextMessageRequest) t;
                final String url = iqWhatsappUrl + endpoints.get(WhatsappMessageType.TEXT.getType());
                return post(url, request.getClientAlias(), request, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED, ex.getMessage(), ex);
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }

    private class MediaMessageHandler implements IWhatsappSenderHandler<WhatsappMessageResponse, WhatsappMessageRequest> {
        public MediaMessageHandler(){}

        @Override
        public WhatsappMessageResponse send (WhatsappMessageRequest t) {
            try {
                final MediaMessageRequest request = (MediaMessageRequest) t;
                final String url = iqWhatsappUrl + endpoints.get(WhatsappMessageType.MEDIA.getType());
                return post(url, request.getClientAlias(), request, WhatsappMessageResponse.class);
            } catch (Exception ex) {
                log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED, ex.getMessage(), ex);
                throw new WynkRuntimeException(SmsErrorType.WHSMS002, ex);
            }
        }
    }
}
