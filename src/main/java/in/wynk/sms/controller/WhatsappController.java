package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.dto.WynkResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wynk/iq/whatsapp")
public class WhatsappController {

    private final ClientDetailsCachingService clientDetailsCachingService;

    @PostMapping(path = "/v1/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @AnalyseTransaction(name = "sendWhatsappSms")
    public WynkResponseEntity<Void> sendWhatsappSms(Principal principal, @RequestBody String payload) {
        Client client = clientDetailsCachingService.getClientById(principal.getName());
        AnalyticService.update(payload);
        return WynkResponseEntity.<Void>builder().status(HttpStatus.OK).build();
    }
}
