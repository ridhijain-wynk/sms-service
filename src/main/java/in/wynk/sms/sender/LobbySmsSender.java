package in.wynk.sms.sender;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.lobby.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service(SMSConstants.LOBBY_MESSAGE_STRATEGY)
public class LobbySmsSender implements IMessageSender<SmsRequest> {

    private final XmlMapper mapper;
    private final RestTemplate smsRestTemplate;
    private final ClientDetailsCachingService clientDetailsCachingService;

    @SneakyThrows
    @Override
    public void sendMessage(SmsRequest request) {
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }

        if (Objects.nonNull(client) && client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_URL").isPresent()) {
            String url = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_URL").get();
            String userName = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_USERNAME").get();
            String accountName = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_ACCOUNT_NAME").get();
            String password = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_PASSWORD").get();
            String shortCode = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_SHORT_CODE").get();
            final SendSmsRequest smsRequest = new SendSmsRequest();
            final Body body = new Body();
            final Form form = new Form();
            final Contact contact = new Contact();
            final Sender sender = new Sender();
            final in.wynk.sms.lobby.Client lobbyClient = new in.wynk.sms.lobby.Client();
            final Wrapper from = new Wrapper();
            contact.setNumber(Long.parseLong(request.getMsisdn().replace("+", "")));
            from.setAccount(accountName);
            from.setUsername(userName);
            from.setPassword(password);
            lobbyClient.setFrom(from);
            lobbyClient.setTo(accountName);
            sender.setNumber(shortCode);
            form.setDa(contact);
            form.setDcs(new Communication());
            form.setOa(sender);
            form.setUd(request.getMessage());
            form.setFrom(lobbyClient);
            body.setSubmit(form);
            smsRequest.setMessage(body);
            final String payload = mapper.writeValueAsString(smsRequest.getMessage());
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            headers.setContentType(MediaType.APPLICATION_XML);
            smsRestTemplate.postForEntity(url, entity, String.class);
        }
    }

}
