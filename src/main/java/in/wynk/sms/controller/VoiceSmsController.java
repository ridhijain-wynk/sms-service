package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BCEncryptor;
import in.wynk.exception.WynkErrorType;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static in.wynk.sms.constants.SMSConstants.SMS_ENCRYPTION_TOKEN;

@RestController
@RequestMapping("/wynk/s2s/v1/voiceSms")
@Slf4j
public class VoiceSmsController {

    private final ISqsManagerService sqsManagerService;
    private final ClientDetailsCachingService clientDetailsCachingService;

    public VoiceSmsController(ISqsManagerService sqsManagerService, ClientDetailsCachingService clientDetailsCachingService) {
        this.sqsManagerService = sqsManagerService;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @AnalyseTransaction(name = "sendVoiceSms")
    @PostMapping("/send")
    public SmsResponse voiceSms(Principal principal, @RequestBody SmsRequest smsRequest) {
        Client client = clientDetailsCachingService.getClientById(principal.getName());
        String msisdn = smsRequest.getMsisdn();
        if (client.getMeta(SMS_ENCRYPTION_TOKEN).isPresent()) {
            msisdn = BCEncryptor.decrypt(smsRequest.getMsisdn(), (String) client.getMeta(SMS_ENCRYPTION_TOKEN).get());
        }
        if (StringUtils.isBlank(msisdn)) {
            throw new WynkRuntimeException(WynkErrorType.UT001, "Invalid msisdn");
        }
        smsRequest.setMsisdn(msisdn);
        AnalyticService.update(smsRequest);
        smsRequest.setClientAlias(client.getAlias());
        sqsManagerService.publishSQSMessage(smsRequest);
        return SmsResponse.builder().build();
    }
}
