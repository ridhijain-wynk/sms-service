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
import in.wynk.sms.queue.message.HighestPriorityMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static in.wynk.sms.constants.SMSConstants.SMS_ENCRYPTION_TOKEN;

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
        Client client = clientDetailsCachingService.getClientById(principal.getName());
        String msisdn = smsRequest.getMsisdn();
        if (client.getMeta(SMS_ENCRYPTION_TOKEN).isPresent()) {
            msisdn = BCEncryptor.decrypt(smsRequest.getMsisdn(), (String) client.getMeta(SMS_ENCRYPTION_TOKEN).get());
        }
        if (StringUtils.isBlank(msisdn)) {
            throw new WynkRuntimeException(WynkErrorType.UT001, "Invalid msisdn");
        }
        smsRequest.setMsisdn(msisdn);
        if (StringUtils.isNotEmpty(smsRequest.getMessage()) && (smsRequest.getMessage().contains("PIN") || smsRequest.getMessage().contains("OTP")))
            smsRequest = HighestPriorityMessage.builder().countryCode(smsRequest.getCountryCode()).msisdn(smsRequest.getMsisdn()).service(smsRequest.getService()).text(smsRequest.getText()).message(smsRequest.getText()).clientAlias(client.getAlias()).shortCode(smsRequest.getShortCode()).messageId(smsRequest.getMsisdn() + System.currentTimeMillis()).build();
        AnalyticService.update(smsRequest);
        smsRequest.setClientAlias(client.getAlias());
        sqsManagerService.publishSQSMessage(smsRequest);
        return SmsResponse.builder().build();
    }
}
