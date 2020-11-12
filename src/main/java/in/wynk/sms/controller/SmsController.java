package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BCEncryptor;
import in.wynk.exception.WynkErrorType;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import in.wynk.sms.model.SendSmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
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

    @AnalyseTransaction(name = "sendSms")
    @PostMapping("/send")
    public SmsResponse sendSms(Principal principal, @RequestBody SmsRequest smsRequest) {
//        String clientAlias = clientDetailsCachingService.getClientById(principal.getName()).getAlias();
//        smsRequest.setService(clientAlias);
//        smsRequest.setShortCode(SMSSource.getShortCode(clientAlias, smsRequest.getPriority()));
//        sqsManagerService.publishSQSMessage(smsRequest);
        String msisdn = BCEncryptor.decrypt(smsRequest.getMsisdn(), BNKRFT_ENCRYPTION_TOKEN);
        if (StringUtils.isBlank(msisdn)) {
            throw new WynkRuntimeException(WynkErrorType.UT001, "Invalid msisdn");
        }
        smsRequest.setMsisdn(msisdn);
        AnalyticService.update(smsRequest);
        sendToOldSystem(smsRequest);
        return SmsResponse.builder().build();
    }

    @Value("${bnkrft.encryption.token:blabla}")
    private String BNKRFT_ENCRYPTION_TOKEN;

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
