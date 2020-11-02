package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import com.google.gson.Gson;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.model.SendSmsResponse;
import in.wynk.sms.model.enums.SMSStatus;
import in.wynk.sms.processor.SMSEnqueueProcessor;
import in.wynk.sms.processor.SMSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsSenderController {

    private static final Logger logger = LoggerFactory.getLogger(SmsSenderController.class);

    @Autowired
    private SMSFactory smsFactory;

    @Autowired
    private SMSEnqueueProcessor smsEnqueueProcessor;


    @PostMapping(value = "/sms/send")
    public SendSmsResponse smsSend(@RequestBody String request) {
        return sendSMS(request);
    }

    @Autowired
    private Gson gson;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @AnalyseTransaction(name = "sendSms")
    public @ResponseBody
    SendSmsResponse sendSMS(@RequestBody String requestStr) {
        AnalyticService.update("request", requestStr);
        SendSmsRequest request = gson.fromJson(requestStr, SendSmsRequest.class);
        SendSmsResponse response = new SendSmsResponse(null, SMSStatus.BAD_REQUEST);
        if (request.validRequest()) {
            try {
                AnalyticService.update("msisdn", request.getMsisdn());
                if (request.getService() != null) {
                    AnalyticService.update("source", request.getService());
                } else {
                    AnalyticService.update("source", request.getSource());
                }
                AnalyticService.update("priority", request.getPriority());
                AnalyticService.update("message", request.getMessage());
                SMSDto sms = smsFactory.getSMSDto(request);
                String countryCode = request.getCountryCode();
                sms.setCountryCode(countryCode);
                AnalyticService.update("countryCode", countryCode);
                logger.info("SMS Request - SMS id : " + sms.getId() + ", createTimestamp : " + sms.getCreationTimestamp() + " ms");
                AnalyticService.update("SMSDto", sms.toString());
                logger.info("processSMSSendRequest");
                smsEnqueueProcessor.processSMSSendRequest(sms);
            } catch (Exception e) {
                logger.error("error while executing sendSMS ", e);
            }
        }
        AnalyticService.update("response status", response.getStatus().name());
        AnalyticService.update("response", response.toString());
        return response;
    }
}
