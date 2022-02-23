package in.wynk.sms.config;

import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.sender.IMessageSender;
import in.wynk.sms.sender.LobbySmsSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SmsSoapConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("in.wynk.sms.lobby");
        return jaxb2Marshaller;
    }

    @Bean(SMSConstants.LOBBY_MESSAGE_STRATEGY)
    public IMessageSender<SmsRequest> lobbyClient(Jaxb2Marshaller jaxb2Marshaller, ClientDetailsCachingService clientDetailsCachingService) {
        final LobbySmsSender lobbyClient = new LobbySmsSender(clientDetailsCachingService);
        lobbyClient.setMarshaller(jaxb2Marshaller);
        lobbyClient.setUnmarshaller(jaxb2Marshaller);
        return lobbyClient;
    }
}
