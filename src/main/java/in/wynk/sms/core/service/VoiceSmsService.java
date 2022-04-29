package in.wynk.sms.core.service;

import in.wynk.sms.dto.request.*;
import in.wynk.sms.dto.response.VoiceSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VoiceSmsService implements IVoiceSmsService {
    @Value("${sms.voice.messages.callFlowId}")
    private String callFlowId;
    @Value("${sms.voice.messages.callerId}")
    private String callerId;
    @Value("${sms.voice.messages.token}")
    private String token;
    @Value("${sms.voice.messages.customerId}")
    private String customerId;
    @Value("${sms.voice.messages.maxRetries}")
    private int maxRetries;
    @Value("${sms.voice.messages.textType}")
    private String textType;
    @Value("${sms.voice.messages.callType}")
    private String callType;
    @Value("${sms.voice.messages.url}")
    private String url;

    private final RestTemplate smsRestTemplate;

    public VoiceSmsService(RestTemplate smsRestTemplate) {
        this.smsRestTemplate = smsRestTemplate;
    }

    @Override
    public VoiceSmsResponse sendVoiceSms(SmsRequest smsRequest) {
        try {
            List<Participant> participants = new ArrayList<>();
            participants.add(Participant.builder().participantAddress(smsRequest.getMsisdn()).maxRetries(maxRetries).build());
            final VoiceSmsRequest request = VoiceSmsRequest.builder().callFlowId(callFlowId).callType(callType)
                    .callFlowId(callFlowId).customerId(customerId).callFlowConfiguration(CallFlowConfiguration.builder()
                    .initiateCall_1(InitiateCall.builder().callerId(callerId).participants(participants).build())
                    .textToSpeech_1(TextToSpeech.builder().text("<speak>" + smsRequest.getText() +"</speak>").textType(textType).build()).build()).build();
            HttpHeaders headers = getHeaders();
            HttpEntity<VoiceSmsRequest> requestEntity = new HttpEntity<>(request, headers);
            VoiceSmsResponse response = smsRestTemplate.exchange(url, HttpMethod.POST, requestEntity, VoiceSmsResponse.class).getBody();
            return response;
        } catch (Exception ex) {
            log.info("failed to send voice sms", ex);
            return null;
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, token);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("textType", "Ssml");
        return headers;
    }

}
