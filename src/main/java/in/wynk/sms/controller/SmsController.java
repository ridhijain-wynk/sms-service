package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BCEncryptor;
import in.wynk.exception.WynkErrorType;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.constant.SMSSource;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/wynk/s2s/v1/sms")
@Slf4j
public class SmsController {

    private final ISqsManagerService sqsManagerService;
    private final ClientDetailsCachingService clientDetailsCachingService;
    @Value("${bnkrft.encryption.token:blabla}")
    private String BNKRFT_ENCRYPTION_TOKEN;

    public SmsController(ISqsManagerService sqsManagerService, ClientDetailsCachingService clientDetailsCachingService) {
        this.sqsManagerService = sqsManagerService;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @AnalyseTransaction(name = "sendSms")
    @PostMapping("/send")
    public SmsResponse sendSms(Principal principal, @RequestBody SmsRequest smsRequest) {
        String msisdn = BCEncryptor.decrypt(smsRequest.getMsisdn(), BNKRFT_ENCRYPTION_TOKEN);
        if (StringUtils.isBlank(msisdn)) {
            throw new WynkRuntimeException(WynkErrorType.UT001, "Invalid msisdn");
        }
        smsRequest.setMsisdn(msisdn);
        AnalyticService.update(smsRequest);
        String clientAlias = clientDetailsCachingService.getClientById(principal.getName()).getAlias();
        smsRequest.setService(clientAlias);
        smsRequest.setShortCode(SMSSource.getShortCode(clientAlias, smsRequest.getPriority()));
        sqsManagerService.publishSQSMessage(smsRequest);
        return SmsResponse.builder().build();
    }
}
