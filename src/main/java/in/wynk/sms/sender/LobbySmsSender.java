package in.wynk.sms.sender;

import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.lobby.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class LobbySmsSender extends WebServiceGatewaySupport implements IMessageSender<SmsRequest> {

    private final ClientDetailsCachingService clientDetailsCachingService;

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
            contact.setNumber(Integer.parseInt(request.getMsisdn().replace("+", "")));
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
            getWebServiceTemplate().marshalSendAndReceive(url, smsRequest);
        }
    }

}
