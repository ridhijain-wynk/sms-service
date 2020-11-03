package in.wynk.sms.controller;

import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.constants.SMSSource;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.SmsResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/wynk/s2s/v1/sms")
public class SmsController {

    private ISqsManagerService sqsManagerService;
    private ClientDetailsCachingService clientDetailsCachingService;

    public SmsController(ISqsManagerService sqsManagerService, ClientDetailsCachingService clientDetailsCachingService) {
        this.sqsManagerService = sqsManagerService;
        this.clientDetailsCachingService = clientDetailsCachingService;
    }

    @PostMapping("/send")
    public SmsResponse sendSms(Principal principal, @RequestBody SmsRequest smsRequest) {
        String clientAlias = clientDetailsCachingService.getClientById(principal.getName()).getAlias();
        smsRequest.setService(clientAlias);
        smsRequest.setShortCode(SMSSource.getShortCode(clientAlias, smsRequest.priority()));
        sqsManagerService.publishSQSMessage(smsRequest);
        return SmsResponse.builder().build();
    }
}
