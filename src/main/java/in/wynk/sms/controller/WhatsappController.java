package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.dto.WynkResponseEntity;
import in.wynk.common.utils.BCEncryptor;
import in.wynk.exception.WynkErrorType;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.dto.request.WhatsappRequest;
import in.wynk.sms.dto.response.SmsResponse;
import in.wynk.sms.queue.message.HighestPriorityMessage;
import in.wynk.sms.service.WhatsappMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static in.wynk.sms.constants.SMSConstants.SMS_ENCRYPTION_TOKEN;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wynk/iq/whatsapp")
public class WhatsappController {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final WhatsappMessageService whatsappMessageService;

    @PostMapping(path = "/v1/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @AnalyseTransaction(name = "sendWhatsappSms")
    public WynkResponseEntity<Void> sendWhatsappSms(Principal principal, @RequestBody WhatsappRequest whatsappRequest) {
        final Client client = clientDetailsCachingService.getClientById(principal.getName());
        AnalyticService.update(whatsappRequest);
        whatsappMessageService.process(whatsappRequest, client.getAlias());
        return WynkResponseEntity.<Void>builder().status(HttpStatus.OK).build();
    }

    @PostMapping(path = "/v1/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    @AnalyseTransaction(name = "iqWhatsappCallback")
    public WynkResponseEntity<Void> iqWhatsappCallback(@RequestBody String payload) {
        try {
            AnalyticService.update(payload);
        } catch(Exception e){
            //eat the exception and return 200 OK
            log.error("Caught exception while processing iq response ", e);
        }
        return WynkResponseEntity.<Void>builder().status(HttpStatus.OK).build();
    }
}
