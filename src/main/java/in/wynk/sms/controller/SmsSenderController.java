package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import com.google.gson.Gson;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.model.SendSmsResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsSenderController {

    private static final Logger logger = LoggerFactory.getLogger(SmsSenderController.class);

    @Autowired
    private ISqsManagerService sqsManagerService;


    @PostMapping(value = "/sms/send")
    public SendSmsResponse smsSend(@RequestBody String request) {
        return sendSMS(request);
    }

    @Autowired
    private Gson gson;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @AnalyseTransaction(name = "sendSms")
    public SendSmsResponse sendSMS(@RequestBody String requestStr) {
        AnalyticService.update("request", requestStr);
        SendSmsRequest request = gson.fromJson(requestStr, SendSmsRequest.class);
        if (request.validRequest()) {
            try {
                if (StringUtils.isNotBlank(request.getSource())) {
                    request.setService(request.getSource());
                }
                if (StringUtils.isNotEmpty(request.getMessage()) && (request.getMessage().contains("PIN") || request.getMessage().contains("pin") || request.getMessage().contains("OTP") || request.getMessage().contains("otp") || request.getMessage().contains("CODE") || request.getMessage().contains("code")))
                    request.setPriority(SMSPriority.HIGHEST.getSmsPriority());
                SmsRequest sms = SMSFactory.getSmsRequest(request);
                AnalyticService.update(sms);
                sqsManagerService.publishSQSMessage(sms);
            } catch (Exception e) {
                logger.error("error while executing sendSMS ", e);
            }
        }
        return SendSmsResponse.defaultResponse();
    }
}
