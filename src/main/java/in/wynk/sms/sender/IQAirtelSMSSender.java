package in.wynk.sms.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.IMessageService;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.dto.request.IQSmsRequest;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.IQSmsResponse;
import in.wynk.sms.enums.SmsErrorType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;

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
    private IMessageService messageService;

    @Autowired
    private SendersCachingService sendersCachingService;

    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;

    @Autowired
    private RestTemplate smsRestTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    @AnalyseTransaction(name = "sendSmsAirtelIQ")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @Override
    public void send(SmsRequest request) {
        try {
            AnalyticService.update(MESSAGE_TEXT, request.getText());
            MessageTemplateDTO messageTemplateDTO = messageService.findMessagesFromSmsText(request.getText());
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
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }
        if(Objects.nonNull(client)){
            final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
            Senders senders = sendersCachingService.getSenderByNameClientCountry(AIRTEL_IQ_SMS_SENDER_BEAN, client.getAlias(), request.getPriority(), countryCode);
            if(Objects.nonNull(senders) && senders.isUrlPresent()){
                IQSmsRequest iqSmsRequest = IQSmsRequest.from(messageTemplateDTO, request, client.getAlias(),
                        senders, Optional.of(senders.getAccountName()).orElse(customerId), Optional.of(senders.getEntityId()).orElse(entityId), countryCode);
                AnalyticService.update(iqSmsRequest);
                if(Objects.isNull(iqSmsRequest.getDltTemplateId()) || Objects.isNull(iqSmsRequest.getSourceAddress())){
                    throw new WynkRuntimeException(SmsErrorType.SMS002);
                }
                try {
                    HttpHeaders headers = new HttpHeaders();
                    String token = Base64.getEncoder().encodeToString((Optional.of(senders.getUsername(messageTemplateDTO.getMessageType())).orElse(this.airtelIqApiUsername) + ":" + Optional.of(
                            senders.getPassword(messageTemplateDTO.getMessageType())).orElse(this.airtelIqApiPassword)).getBytes());
                    headers.add(AUTHORIZATION, "Basic " + token);
                    headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    URI uri = new URI(Optional.of(senders.getUrl(messageTemplateDTO.getMessageType())).orElse(this.airtelIqApiUrl));
                    HttpEntity<IQSmsRequest> requestEntity = new HttpEntity<>(iqSmsRequest, headers);
                    AnalyticService.update("senderUrl", uri.toString());
                    ResponseEntity<IQSmsResponse> responseEntity = smsRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, IQSmsResponse.class);
                    IQSmsResponse response = responseEntity.getBody();
                    AnalyticService.update(HTTP_STATUS_CODE, responseEntity.getStatusCode().name());
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
    }
}
