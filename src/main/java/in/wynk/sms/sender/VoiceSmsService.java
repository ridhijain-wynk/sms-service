package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.request.*;
import in.wynk.sms.dto.response.VoiceSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service(SMSBeanConstant.AIRTEL_VOICE_MESSAGE_SENDER)
public class VoiceSmsService extends AbstractSMSSender {

    @Value("${sms.voice.messages.callFlowId}")
    private String CALL_FLOW_ID;
    @Value("${sms.voice.messages.callerId}")
    private String CALLER_ID;
    @Value("${sms.voice.messages.token}")
    private String TOKEN;
    @Value("${sms.voice.messages.customerId}")
    private String CUSTOMER_ID;
    @Value("${sms.voice.messages.maxRetries}")
    private int MAX_RETRIES;
    @Value("${sms.voice.messages.textType}")
    private String TEXT_TYPE;
    @Value("${sms.voice.messages.callType}")
    private String CALL_TYPE;
    @Value("${sms.voice.messages.url}")
    private String URL;

    private final RestTemplate smsRestTemplate;
    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SendersCachingService sendersCachingService;

    public VoiceSmsService(RestTemplate smsRestTemplate, ClientDetailsCachingService clientDetailsCachingService, SendersCachingService sendersCachingService) {
        this.smsRestTemplate = smsRestTemplate;
        this.clientDetailsCachingService = clientDetailsCachingService;
        this.sendersCachingService = sendersCachingService;
    }

    @Override
    @AnalyseTransaction(name = "airtelVoiceSender")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @Override
    public void send(SmsRequest request) {
        try {
            AnalyticService.update(request);
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }

            Optional<String> callFlowIdOption = Optional.empty();
            Optional<String> callerIdOption = Optional.empty();
            Optional<String> tokenOption = Optional.empty();
            Optional<String> customerIdOption = Optional.empty();
            Optional<Integer> maxRetriesOption = Optional.empty();
            Optional<String> textTypeOption = Optional.empty();
            Optional<String> callTypeOption = Optional.empty();
            Optional<String> urlOption = Optional.empty();

            final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
            Senders senders = sendersCachingService.getSenderByNameClientCountry(SMSBeanConstant.AIRTEL_VOICE_MESSAGE_SENDER, client.getAlias(), request.getPriority(), countryCode);
            if (Objects.nonNull(senders) && Objects.nonNull(senders.getVoice()) && senders.isUrlPresent()) {
                urlOption = Optional.of(senders.getUrl());
                callTypeOption = Optional.of(senders.getVoice().getCallType());
                textTypeOption = Optional.of(senders.getVoice().getTextType());
                maxRetriesOption = Optional.of(senders.getVoice().getMaxRetry());
                customerIdOption = Optional.of(senders.getVoice().getCustomerId());
                tokenOption = Optional.of(senders.getVoice().getToken());
                callerIdOption = Optional.of(senders.getVoice().getCallerId());
                callFlowIdOption = Optional.of(senders.getVoice().getCallFlowId());
            }


            final List<Participant> participants = new ArrayList<>();
            participants.add(Participant.builder().participantAddress(request.getMsisdn()).maxRetries(maxRetriesOption.orElse(MAX_RETRIES)).build());
            final VoiceSmsRequest voiceSmsRequest = VoiceSmsRequest.builder().callFlowId(callFlowIdOption.orElse(CALL_FLOW_ID)).callType(callTypeOption.orElse(CALL_TYPE))
                    .callFlowId(callFlowIdOption.orElse(CALL_FLOW_ID)).customerId(customerIdOption.orElse(CUSTOMER_ID)).callFlowConfiguration(CallFlowConfiguration.builder()
                            .initiateCall_1(InitiateCall.builder().callerId(callerIdOption.orElse(CALLER_ID)).participants(participants).build())
                            .textToSpeech_1(TextToSpeech.builder().text(updateTextMsg(request.getText())).textType(textTypeOption.orElse(TEXT_TYPE)).build()).build()).build();
            final HttpHeaders headers = getHeaders(tokenOption);
            final HttpEntity<VoiceSmsRequest> requestEntity = new HttpEntity<>(voiceSmsRequest, headers);
            ResponseEntity<VoiceSmsResponse> response = smsRestTemplate.exchange(urlOption.orElse(URL), HttpMethod.POST, requestEntity, VoiceSmsResponse.class);
            AnalyticService.update(response.getBody());
            AnalyticService.update(BaseConstants.HTTP_STATUS_CODE, response.getStatusCode().name());
        } catch (Exception ex) {
            throw new WynkRuntimeException("failed to send voice sms", ex);
        }
    }

    private String updateTextMsg(String txt) {
        String text[] = txt.split(" ");
        StringBuilder finalTxt = new StringBuilder();
        for (int i = 0; i < text.length; i++) {
            isNumeric(text[i], i, text);
            finalTxt.append(text[i] + " ");
        }
        return "<speak>" + finalTxt.toString() + "</speak>";

    }

    public static boolean isNumeric(String strNum, int idx, String txt[]) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        StringBuilder otp = new StringBuilder();
        char nums[] = strNum.toCharArray();
        for (int i = 0; i < nums.length; i++) {
            otp.append(nums[i] + " ");
        }
        txt[idx] = otp.toString();
        return true;
    }

    private HttpHeaders getHeaders(Optional<String> tokenOption) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, tokenOption.orElse(TOKEN));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("textType", "Ssml");
        return headers;
    }

}
