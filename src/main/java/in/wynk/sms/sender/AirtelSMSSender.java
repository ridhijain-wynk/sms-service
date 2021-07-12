package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.common.constant.SMSSource;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public AirtelSMSSender(RestTemplate smsRestTemplate) {
        this.smsRestTemplate = smsRestTemplate;
    }

    @Override
    @AnalyseTransaction(name = "sendSmsAirtel")
    public void sendMessage(SmsRequest request) throws Exception {
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
                    smsRestTemplate.exchange(requestEntity, String.class);
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
        String url = "https://mbnf.airtelworld.com:8443";
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
        if (Objects.nonNull(client) && client.getMeta(smsRequest.getPriority().name() + "_PRIORITY_SMS_URL").isPresent()) {
            String url = (String) client.getMeta(smsRequest.getPriority().name() + "_PRIORITY_SMS_URL").get();
            String userName = (String) client.getMeta(smsRequest.getPriority().name() + "_PRIORITY_SMS_USERNAME").get();
            String password = (String) client.getMeta(smsRequest.getPriority().name() + "_PRIORITY_SMS_PASSWORD").get();
            String shortCode = (String) client.getMeta(smsRequest.getPriority().name() + "_PRIORITY_SMS_SHORT_CODE").get();
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
        String url = "https://mbbf.airtelworld.com:8443";
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
        if (!isEnglish(smsRequest.getText())) {
            strBuilder.append(" encoding=\"unicode\"");
        }
        strBuilder.append(">");
        // String is converted to hexString to support non english text too.
        strBuilder.append(StringEscapeUtils.escapeXml(smsRequest.getText())).append("</ud>");
        strBuilder.append("</sms></message>");
        return strBuilder.toString();
    }

    private boolean isEnglish(String text) {
        for (int i=0; i<text.length(); ++i) {
            int asciiValue = text.charAt(i);
            if (asciiValue <33 || asciiValue > 126) {
                return false;
            }
        }
        return true;
    }

}