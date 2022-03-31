package in.wynk.sms.sender;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service(SMSConstants.LOBBY_MESSAGE_STRATEGY)
public class LobbySmsSender implements IMessageSender<SmsRequest> {

    private final XmlMapper mapper = new XmlMapper();
    private final RestTemplate smsRestTemplate;
    private final ClientDetailsCachingService clientDetailsCachingService;

    @SneakyThrows
    @Override
    @AnalyseTransaction(name = "sendSmsLobby")
    public void sendMessage(SmsRequest request) {
        AnalyticService.update(request);
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }

        if (Objects.nonNull(client) && client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_URL").isPresent()) {
            String url = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_URL").get();
            String userName = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_USERNAME").get();
            String accountName = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_ACCOUNT_NAME").orElse("Testnorth");
            String password = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_PASSWORD").get();
            String shortCode = (String) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_SHORT_CODE").get();
            int mclass = (Integer) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_MCLASS").orElse(1);
            int coding = (Integer) client.getMeta(request.getPriority().name() + "_PRIORITY_SMS_CODING").orElse(0);
            final Body body = new Body();
            final Form form = new Form();
            final Contact contact = new Contact();
            final Sender sender = new Sender();
            final Wrapper from = new Wrapper();
            final Communication comm = new Communication();
            contact.setNumber(Long.parseLong(request.getMsisdn().replace("+", "")));
            from.setAccount(accountName);
            from.setUsername(userName);
            from.setPassword(password);
            form.setFrom(from);
            form.setTo(accountName);
            sender.setNumber(shortCode);
            form.setDa(contact);
            comm.setMclass(mclass);
            comm.setCoding(coding);
            form.setDcs(comm);
            form.setOa(sender);
            form.setUd(request.getText());
            body.setSubmit(form);
            final String payload = mapper.writeValueAsString(body);
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/xml");
            smsRestTemplate.postForEntity(url, entity, String.class);
        }
    }

}
