package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BCEncryptor;
import in.wynk.exception.WynkErrorType;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.pubsub.service.IPubSubManagerService;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import in.wynk.sms.pubsub.message.HighestPriorityGCPMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static in.wynk.sms.constants.SMSConstants.SMS_ENCRYPTION_TOKEN;

@Slf4j
@RestController
@RequestMapping({"/wynk/s2s", "/iq/s2s/message"})
public class SmsController {

    private final ISqsManagerService<Object> sqsManagerService;

    private final IPubSubManagerService<Object> pubSubManagerService;
    private final ClientDetailsCachingService clientDetailsCachingService;

    public SmsController(ISqsManagerService<Object> sqsManagerService, IPubSubManagerService<Object> pubSubManagerService, ClientDetailsCachingService clientDetailsCachingService) {
        this.sqsManagerService = sqsManagerService;
        this.pubSubManagerService = pubSubManagerService;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @PostMapping({"/v1/voiceSms/send", "/v1/voice/send"})
    public SmsResponse sendVoiceSms(Principal principal, @RequestBody SmsRequest smsRequest) {
        smsRequest.setCommunicationType(CommunicationType.VOICE);
        return sendSms(principal, smsRequest);
    }

    @PostMapping("/v1/sms/send")
    @AnalyseTransaction(name = "sendSms")
    public SmsResponse sendSms(Principal principal, @RequestBody SmsRequest smsRequest) {
        Client client = clientDetailsCachingService.getClientById(principal.getName());
        String msisdn = smsRequest.getMsisdn();
        if (client.getMeta(SMS_ENCRYPTION_TOKEN).isPresent()) {
            msisdn = BCEncryptor.decrypt(smsRequest.getMsisdn(), (String) client.getMeta(SMS_ENCRYPTION_TOKEN).get());
        }
        if (StringUtils.isBlank(msisdn)) {
            throw new WynkRuntimeException(WynkErrorType.UT001, "Invalid msisdn");
        }
        smsRequest.setMsisdn(msisdn);
        if (StringUtils.isNotEmpty(smsRequest.getText()) && (smsRequest.getText().contains("PIN") || smsRequest.getText().contains("pin") || smsRequest.getText().contains("OTP") || smsRequest.getText().contains("otp") || smsRequest.getText().contains("CODE") || smsRequest.getText().contains("code")))
            smsRequest = HighestPriorityGCPMessage.builder().countryCode(smsRequest.getCountryCode()).communicationType(smsRequest.getCommunicationType()).msisdn(smsRequest.getMsisdn()).service(smsRequest.getService()).text(smsRequest.getText()).message(smsRequest.getText()).shortCode(smsRequest.getShortCode()).messageId(smsRequest.getMsisdn() + System.currentTimeMillis()).build();
        AnalyticService.update(smsRequest);
        smsRequest.setClientAlias(client.getAlias());
       // sqsManagerService.publishSQSMessage(smsRequest);
        pubSubManagerService.publishPubSubMessage(smsRequest);
        return SmsResponse.builder().build();
    }

}
