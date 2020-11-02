package in.wynk.sms.controller;

import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.request.SmsResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wynk/s2s/v1/sms")
public class SmsController {

    private ISqsManagerService sqsManagerService;

    public SmsController(ISqsManagerService sqsManagerService) {
        this.sqsManagerService = sqsManagerService;
    }

    @PostMapping("/send")
    public SmsResponse sendSms(SmsRequest smsRequest) {
        sqsManagerService.publishSQSMessage(smsRequest);
        return SmsResponse.builder().build();
    }
}
