package in.wynk.sms.controller;

import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import in.wynk.sms.model.SendSmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static in.wynk.sms.constants.SmsMarkers.SMS_ERROR;

@RestController
@RequestMapping("/wynk/s2s/v1/sms")
@Slf4j
public class SmsController {

    private final ISqsManagerService sqsManagerService;
    private final ClientDetailsCachingService clientDetailsCachingService;

    public SmsController(ISqsManagerService sqsManagerService, ClientDetailsCachingService clientDetailsCachingService) {
        this.sqsManagerService = sqsManagerService;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @PostMapping("/send")
    public SmsResponse sendSms(Principal principal, @RequestBody SmsRequest smsRequest) {
//        String clientAlias = clientDetailsCachingService.getClientById(principal.getName()).getAlias();
//        smsRequest.setService(clientAlias);
//        smsRequest.setShortCode(SMSSource.getShortCode(clientAlias, smsRequest.getPriority()));
//        sqsManagerService.publishSQSMessage(smsRequest);
        sendToOldSystem(smsRequest);
        return SmsResponse.builder().build();
    }

    //TODO: temporary for bunkrft
    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setReadTimeout(Duration.of(2, ChronoUnit.SECONDS))
            .setConnectTimeout(Duration.of(1, ChronoUnit.SECONDS)).build();

    private void sendToOldSystem(SmsRequest smsRequest) {
        SendSmsRequest oldSmsRequest = SMSFactory.getOldSendSmsRequest(smsRequest);
        String oldSmsServiceURL = "http://sms.wcf.internal/sms/send";
        URI uri = null;
        try {
            uri = new URIBuilder(oldSmsServiceURL).build();
        } catch (URISyntaxException e) {
            log.error(SMS_ERROR, e.getMessage(), e);
        }
        RequestEntity<SendSmsRequest> requestEntity = new RequestEntity<>(oldSmsRequest, HttpMethod.POST, uri);
        restTemplate.exchange(requestEntity, String.class);

    }
}
