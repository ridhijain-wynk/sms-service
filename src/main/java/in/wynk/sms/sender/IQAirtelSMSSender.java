package in.wynk.sms.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.core.service.IMessageTemplateService;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.dto.request.IQSmsRequest;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.IQSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;
import static in.wynk.sms.constants.SMSConstants.*;
import static in.wynk.sms.constants.SmsLoggingMarkers.NO_TEMPLATE_FOUND;
import static in.wynk.sms.constants.SmsLoggingMarkers.SMS_ERROR;
import static in.wynk.sms.enums.SmsErrorType.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
@Component(AIRTEL_IQ_SMS_SENDER_BEAN)
public class IQAirtelSMSSender extends AbstractSMSSender {

    @Value("${sms.airtel.iq.customerId}")
    private String customerId;

    @Value("${sms.airtel.iq.entityId}")
    private String entityId;

    @Value("${sms.airtel.iq.url}")
    private String airtelIqApiUrl;

    @Value("${sms.airtel.iq.username}")
    private String airtelIqApiUsername;

    @Value("${sms.airtel.iq.password}")
    private String airtelIqApiPassword;

    @Autowired
    private IMessageTemplateService messageTemplateService;

    @Autowired
    private RestTemplate smsRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    @AnalyseTransaction(name = "sendSmsAirtelIQ")
    public void sendMessage(SmsRequest request) {
        try {
            AnalyticService.update(MESSAGE_TEXT, request.getText());
            MessageTemplateDTO messageTemplateDTO = messageTemplateService.findMessageTemplateFromSmsText(request.getText());
            if (Objects.isNull(messageTemplateDTO)) {
                log.error(NO_TEMPLATE_FOUND, "No template found for message: {}", request.getText());
                throw new WynkRuntimeException(IQSMS001);
            }
            sendSmsThroughAirtelIQ(request, messageTemplateDTO);
        } catch (WynkRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(SMS_ERROR, "sms error for messageId :" + request.getMessageId() + "for msisdn " + request.getMsisdn(), ex);
            throw new WynkRuntimeException(IQSMS002, ex);
        }
    }

    private void sendSmsThroughAirtelIQ(SmsRequest request, MessageTemplateDTO messageTemplateDTO) throws URISyntaxException {
        IQSmsRequest iqSmsRequest = IQSmsRequest.from(messageTemplateDTO, request, customerId, entityId);
        AnalyticService.update(iqSmsRequest);
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = Base64.getEncoder().encodeToString((airtelIqApiUsername + ":" + airtelIqApiPassword).getBytes());
            headers.add(AUTHORIZATION, "Basic " + token);
            headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            URI uri = new URI(airtelIqApiUrl);
            HttpEntity<IQSmsRequest> requestEntity = new HttpEntity<>(iqSmsRequest, headers);
            IQSmsResponse response = smsRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, IQSmsResponse.class).getBody();
            AnalyticService.update(response);
        } catch (HttpStatusCodeException ex) {
            try {
                if (ex.getStatusCode() == HttpStatus.BAD_REQUEST && Objects.nonNull(ex.getResponseBodyAsString())) {
                    final Map<String, String> failureResponse = mapper.readValue(ex.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {
                    });
                    if (MapUtils.isNotEmpty(failureResponse) && failureResponse.containsKey(FAILURE_CODE) && failureResponse.get(FAILURE_CODE).equalsIgnoreCase(TIME_NOT_VALID_FOR_MESSAGE_TYPE)) {
                        AnalyticService.update(FAILURE_CODE, TIME_NOT_VALID_FOR_MESSAGE_TYPE);
                        return;
                    }
                }
                throw new WynkRuntimeException(IQSMS003, ex);
            } catch (JsonProcessingException jpe) {
                log.error(APPLICATION_ERROR, "unable to parse failure response due to {}", jpe.getMessage(), jpe);
            }
        } catch (Exception ex) {
            log.error("External service failure due to {}", ex.getMessage(), ex);
            throw new WynkRuntimeException(IQSMS003, ex);
        }
    }


}
