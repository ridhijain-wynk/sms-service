package in.wynk.sms.controller;

import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.core.service.MessageServiceV2;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.core.service.SendersCachingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("!prod")
@RequiredArgsConstructor
@RequestMapping("/wynk/s2s/v1")
public class CacheRefreshController {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final MessageCachingService messageCachingService;
    private final SenderConfigurationsCachingService senderConfigurationsCachingService;
    private final SendersCachingService sendersCachingService;
    private final MessageServiceV2 messageServiceV2;

    @GetMapping("/cache/refresh")
    public void refreshCache() {
        clientDetailsCachingService.init();
        messageCachingService.init();
        senderConfigurationsCachingService.init();
        sendersCachingService.init();
        messageServiceV2.init();
    }

}