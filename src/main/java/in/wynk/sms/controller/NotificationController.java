package in.wynk.sms.controller;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.common.dto.WynkResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wynk/s2s/iq")
public class NotificationController {

    @AnalyseTransaction(name = "inboundNotification")
    @PostMapping(path = "/v1/notifications/inbound/{clientAlias}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WynkResponseEntity<Void> handleNotification(@PathVariable String clientAlias, @RequestBody String payload) {
        AnalyticService.update(BaseConstants.CLIENT_ID, clientAlias);
        AnalyticService.update(BaseConstants.PAYLOAD, payload);
        return WynkResponseEntity.<Void>builder().build();
    }

    @AnalyseTransaction(name = "messageStatusCallback")
    @PostMapping(path = "/v1/callback/message-status/{clientAlias}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WynkResponseEntity<Void> handleCallback(@PathVariable String clientAlias, @RequestBody String payload) {
        AnalyticService.update(BaseConstants.CLIENT_ID, clientAlias);
        AnalyticService.update(BaseConstants.PAYLOAD, payload);
        return WynkResponseEntity.<Void>builder().build();
    }


}
