package in.wynk.sms.sender;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.lobby.*;
import in.wynk.sms.utils.SMSUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.LOBBY_MESSAGE_STRATEGY;

@Slf4j
@RequiredArgsConstructor
@Service(LOBBY_MESSAGE_STRATEGY)
public class LobbySmsSender extends AbstractSMSSender {

    private final XmlMapper mapper = new XmlMapper();
    private final RestTemplate smsRestTemplate;
    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SendersCachingService sendersCachingService;

    @Override
    @AnalyseTransaction(name = "sendSmsLobby")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @SneakyThrows
    @Override
    public void send(SmsRequest request) {
        AnalyticService.update(request);
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }
        final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
        Senders senders = sendersCachingService.getSenderByNameClientCountry(LOBBY_MESSAGE_STRATEGY, client.getAlias(), request.getPriority(), countryCode);
        if(Objects.nonNull(senders) && senders.isUrlPresent()){
            String url = senders.getUrl();
            String userName = senders.getUsername();
            String accountName = Objects.nonNull(senders.getAccountName()) ? senders.getAccountName() : "Testnorth";
            String password = senders.getPassword();
            String shortCode = SMSUtils.getShortCode(request.getTemplateId(), request.getPriority(), client.getAlias(), senders.getShortCode(), countryCode);
            shortCode=SMSUtils.getSuffixedShortCode(request.getTemplateId(), shortCode,request.getPriority());
            int mClass = request.isEnglish() ? 1: Objects.nonNull(senders.getMClass()) ? Integer.parseInt(senders.getMClass()) : 2;
            int coding = Objects.nonNull(senders.getCoding()) ? senders.getCoding() : 0;
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
            comm.setMclass(mClass);
            comm.setCoding(coding);
            form.setDcs(comm);
            form.setOa(sender);
            form.setUd(request.getText());
            body.setSubmit(form);
            final String payload = mapper.writeValueAsString(body);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = smsRestTemplate.postForEntity(url, entity, String.class);
            AnalyticService.update(BaseConstants.HTTP_STATUS_CODE, response.getStatusCode().name());
        }
    }

}
