package in.wynk.sms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.dto.WynkResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wynk/iq/sms")
public class DeliveryReportController {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/v1/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    @AnalyseTransaction(name = "IQSMSCallback")
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