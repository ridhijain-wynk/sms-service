package in.wynk.sms.processor;


import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.model.SMSDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SMSEnqueueProcessor {

    @Autowired
    private ISqsManagerService sqsManagerService;


    public void processSMSSendRequest(SMSDto sms) {
        sqsManagerService.publishSQSMessage(sms);
    }

}
