package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.constant.SMSSource;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static in.wynk.common.constant.BaseConstants.HTTP_STATUS_CODE;
import static in.wynk.sms.constants.SMSConstants.AIRTEL_SMS_SENDER;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;

@Component(AIRTEL_SMS_SENDER)
@Slf4j
public class AirtelSMSSender extends AbstractSMSSender {


    private static final Map<String, String> htmlEntityMap = new HashMap<>();


    static {
        htmlEntityMap.put("&", "&amp;");
        htmlEntityMap.put("<", "&lt;");
        htmlEntityMap.put(">", "&gt;");
    }

    private final RestTemplate smsRestTemplate;
    @Autowired
    private ClientDetailsCachingService clientDetailsCachingService;
    @Autowired
    private SendersCachingService sendersCachingService;

    public AirtelSMSSender(RestTemplate smsRestTemplate) {
        this.smsRestTemplate = smsRestTemplate;
    }

    @Override
    @AnalyseTransaction(name = "sendSmsAirtel")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @Override
    public void send(SmsRequest request) throws Exception {
        AnalyticService.update(request);
        Country country = Country.getCountryByCountryCode(request.getCountryCode());
        if (country.equals(Country.SRILANKA)) {
            sendSmsToSriLanka(request);
        } else {
            sendSms(request);
        }
    }

    private void sendSms(SmsRequest smsRequest) throws Exception {
        if (!postSMS(smsRequest)) {
            String shortCode = SMSSource.getShortCode(smsRequest.getService(), smsRequest.getPriority());
            final String countryCode = StringUtils.isNotEmpty(smsRequest.getCountryCode()) ? Country.getCountryIdByCountryCode(smsRequest.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
            shortCode = SMSUtils.getShortCode(smsRequest.getTemplateId(), smsRequest.getPriority(), smsRequest.getClientAlias(), shortCode, countryCode);
            String mtRequestXML = createMTRequestXML(shortCode, smsRequest);
            postCoreJava(mtRequestXML, smsRequest.getMsisdn(), smsRequest.getPriority());
        }
    }

    public void sendSmsToSriLanka(SmsRequest smsRequest) {
        if (smsRequest != null) {
            String requestUrl = "http://sms.airtel.lk:5000/sms/send_sms.php?username=wynk&password=W123Nk&src=Wynk&dr=1";
            if (StringUtils.isNotBlank(smsRequest.getMsisdn())) {
                requestUrl = requestUrl.concat("&dst=").concat(smsRequest.getMsisdn().replace("+", ""));
            }
            if (StringUtils.isNotBlank(smsRequest.getText())) {
                try {
                    requestUrl = requestUrl.concat("&msg=");
                    requestUrl = requestUrl.concat(URLEncoder.encode(smsRequest.getText(), "UTF-8"));
                    org.springframework.http.HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
                    RequestEntity<String> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, new URI(requestUrl));
                    ResponseEntity<String> response = smsRestTemplate.exchange(requestEntity, String.class);
                    AnalyticService.update(HTTP_STATUS_CODE, response.getStatusCode().name());
                } catch (Throwable th) {
                    logger.error(SL_SMS_ERROR, "Error Sending SMS to Msisdn: {" + smsRequest.getMsisdn() + "} ERROR: { " + th.getMessage() + "}", th);
                }
            }
        }
    }

    private String filterHtmlEntity(String text) {
        for (Map.Entry<String, String> m : htmlEntityMap.entrySet()) {
            text = text.replaceAll(m.getKey(), m.getValue());
        }
        return text;
    }

    private void postCoreJava(String dataXml, String msisdn, SMSPriority priority) {
        if (priority == SMSPriority.HIGH) {
            postSmsForHighPriority(dataXml, msisdn);
        } else {
            postSmsForLowAndMediumPriority(dataXml, msisdn);
        }
    }

    private void postSmsForHighPriority(String dataXml, String msisdn) {
        String url = "https://mbnf.airtelworld.com:9443";
        AnalyticService.update("url", url);
        final String username = "bs1b";
        final String password = "bs1b";
        try {
            sendSmsRequestToAirtel(dataXml, url, username, password);
        } catch (Exception e) {
            AnalyticService.update("smsAirtelException", e.toString());
            logger.error(HIGH_PRIORITY_SMS_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
    }

    private boolean postSMS(SmsRequest smsRequest) throws Exception {
        Client client = clientDetailsCachingService.getClientByAlias(smsRequest.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(smsRequest.getService());
        }
        final String countryCode = StringUtils.isNotEmpty(smsRequest.getCountryCode()) ? Country.getCountryIdByCountryCode(smsRequest.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
        Senders senders = sendersCachingService.getSenderByNameClientCountry(AIRTEL_SMS_SENDER, client.getAlias(), smsRequest.getPriority(), countryCode);
        if (Objects.nonNull(senders) && senders.isUrlPresent()){
            String url = senders.getUrl();
            String userName = senders.getUsername();
            String password = senders.getPassword();
            String shortCode = senders.getShortCode();
            shortCode = SMSUtils.getShortCode(smsRequest.getTemplateId(), smsRequest.getPriority(), smsRequest.getClientAlias(), shortCode, countryCode);
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.isNoneBlank(url, shortCode)) {
                if (StringUtils.isNoneBlank(userName, password)) {
                    headers.setBasicAuth(userName, password);
                }
                String dataXml = createMTRequestXML(shortCode, smsRequest);
                sendSmsRequestToAirtel(dataXml, url, headers);
                return true;
            }
        }
        //Temporary returning boolean as fallback for old pipeline.
        return false;
    }

    private void postSmsForLowAndMediumPriority(String dataXml, String msisdn) {
        String url = "https://mbbf.airtelworld.com:9443";
        AnalyticService.update("url", url);
        final String username = "wynk12";
        final String password = "12wynk";
        try {
            sendSmsRequestToAirtel(dataXml, url, username, password);
        } catch (Exception e) {
            logger.error(PROMOTIONAL_MSG_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
    }

    private void sendSmsRequestToAirtel(String dataXml, String url, HttpHeaders headers) throws Exception {
        headers.setContentType(MediaType.TEXT_XML);
        RequestEntity<String> requestEntity = new RequestEntity<>(dataXml, headers, HttpMethod.POST, new URI(url));
        smsRestTemplate.exchange(requestEntity, String.class);
    }

    private void sendSmsRequestToAirtel(String dataXml, String url, String username, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.TEXT_XML);
        RequestEntity<String> requestEntity = new RequestEntity<>(dataXml, headers, HttpMethod.POST, new URI(url));
        smsRestTemplate.exchange(requestEntity, String.class);
    }

    private String createMTRequestXML(String shortCode, SmsRequest smsRequest) {
        if (smsRequest == null) {
            return null;
        }
        String toMsisdn = smsRequest.getMsisdn().startsWith("+") ? smsRequest.getMsisdn().substring(1) : smsRequest.getMsisdn();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<?xml version=\"1.0\" standalone=\"yes\"?>");
        strBuilder.append("<message>");
        strBuilder.append("<sms type=\"mt\">");
        if (StringUtils.isNotBlank(smsRequest.getMessageId())) {
            strBuilder.append("<destination messageid=\"").append(smsRequest.getMessageId()).append("\">");
        } else {
            strBuilder.append("<destination>");
        }
        strBuilder.append("<address>");
        strBuilder.append("<number type=\"international\">").append(toMsisdn).append("</number>");
        strBuilder.append("</address></destination>");
        strBuilder.append("<source><address>").append("<alphanumeric>").append(shortCode).append("</alphanumeric></address></source>");
        strBuilder.append("<rsr type=\"all\"/>");
        strBuilder.append("<ud type=\"text\"");
        if (!smsRequest.isEnglish()) {
            strBuilder.append(" encoding=\"unicode\"");
        } else {
            strBuilder.append(" encoding=\"default\"");
        }
        strBuilder.append(">");
        // String is converted to hexString to support non english text too.
        strBuilder.append(StringEscapeUtils.escapeXml(smsRequest.getText())).append("</ud>");
        strBuilder.append("</sms></message>");
        return strBuilder.toString();
    }
    
}