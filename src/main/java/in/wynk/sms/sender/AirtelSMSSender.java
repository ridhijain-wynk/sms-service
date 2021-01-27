package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import static in.wynk.sms.constants.SmsMarkers.*;

@Component
@Slf4j
public class AirtelSMSSender extends AbstractSMSSender {


    private static final Map<String, String> htmlEntityMap = new HashMap<>();


    static {
        htmlEntityMap.put("&", "&amp;");
        htmlEntityMap.put("<", "&lt;");
        htmlEntityMap.put(">", "&gt;");
    }

    private final RestTemplate smsRestTemplate;


    public AirtelSMSSender(RestTemplate smsRestTemplate) {
        this.smsRestTemplate = smsRestTemplate;
    }

    @Override
    @AnalyseTransaction(name = "sendSmsAirtel")
    public void sendMessage(SmsRequest request) {
        AnalyticService.update(request);
        Country country = Country.getCountryByCountryCode(request.getCountryCode());
        if (country.equals(Country.SRILANKA)) {
            sendSmsToSriLanka(request);
        }
        sendMessage(request.getMsisdn(), request.getShortCode(), request.getText(), request.getPriority().name(), request.getMessageId());
    }


    private void sendMessage(String msisdn, String shortCode, String text, String priority, String smsId) {
        try {
            AnalyticService.update("message", text);
            SMSMsg sms = new SMSMsg();
            sms.shortcode = shortCode;
            sms.toMsisdn = msisdn;
            sms.message = filterHtmlEntity(text);
            sms.messageId = smsId;
            String mtRequestXML = createMTRequestXML(sms);
            postCoreJava(mtRequestXML, msisdn, priority, smsId);
        } catch (Throwable th) {
            logger.error("Error while Delivering SMS, ERROR: {}", th.getMessage(), th);
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

    private void postCoreJava(String dataXml, String msisdn, String priority, String id) {
        AnalyticService.update("msisdn", msisdn);
        AnalyticService.update("smsAirtelRequest", dataXml);
        AnalyticService.update("priority", priority);
        if (priority.equals(SMSPriority.HIGH.name())) {
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
            logger.error(TRANSACTIONAL_MSG_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
    }

    private void postSmsForLowAndMediumPriority(String dataXml, String msisdn) {
        String url = "https://mbbf.airtelworld.com:9443";
        AnalyticService.update("url", url);
        final String username = "wynk12";
        final String password = "12wynk";
        try {
            sendSmsRequestToAirtel(dataXml, url, username, password);
        } catch (Exception e) {
            AnalyticService.update("smsAirtelException", e.toString());
            logger.error(PROMOTIONAL_MSG_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
    }

    private void sendSmsRequestToAirtel(String dataXml, String url, String username, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.TEXT_XML);
        RequestEntity<String> requestEntity = new RequestEntity<>(dataXml, headers, HttpMethod.POST, new URI(url));
        smsRestTemplate.exchange(requestEntity, String.class);
    }

    private String createMTRequestXML(SMSMsg mrObject) {
        if (mrObject == null) {
            return null;
        }
        String toMsisdn = mrObject.getToMsisdn().startsWith("+") ? mrObject.getToMsisdn().substring(1) : mrObject.getToMsisdn();
        StringBuilder strBuilder = new StringBuilder();
        if(StringUtils.isAsciiPrintable(mrObject.message)){
            strBuilder.append("<?xml version=\"1.0\" standalone=\"yes\"?>");
        }else {
            strBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        }
        strBuilder.append("<message>");
        strBuilder.append("<sms type=\"mt\">");
        if (mrObject.messageId != null && !mrObject.messageId.isEmpty()) {
            strBuilder.append("<destination messageid=\"").append(mrObject.messageId).append("\">");
        } else {
            strBuilder.append("<destination>");
        }
        strBuilder.append("<address>");
        strBuilder.append("<number type=\"international\">").append(toMsisdn).append("</number>");
        strBuilder.append("</address></destination>");
        if (mrObject.shortcode != null) {
            strBuilder.append("<source><address>").append("<alphanumeric>").append(mrObject.shortcode).append("</alphanumeric></address></source>");
        } else if (mrObject.getFromMsisdn() != null) {
            strBuilder.append("<source><address>").append(mrObject.getFromMsisdn()).append("</address></source>");
        }
        strBuilder.append("<rsr type=\"all\"/>");
        if(StringUtils.isAsciiPrintable(mrObject.message)){
            strBuilder.append("<ud type=\"").append(mrObject.udType).append("\">");
        }else {
            strBuilder.append("<ud encoding=\"unicode\" type=\"").append(mrObject.udType).append("\">");
        }
        // String is converted to hexString to support non english text too.
        strBuilder.append(convertToHexString(mrObject.message, false)[1]).append("</ud>");
        strBuilder.append("</sms></message>");
        return strBuilder.toString();
    }


    class SMSMsg {

        public final String rsr = "all";     // all | failure | success | delayed |
        public final String udType = "text";    // text or bindary
        // success_failure | success_delayed |
        // failure_delayed | sent | sent_delivered
        public final String udEncoding = "default"; // unicode or default
        public final String vpType = "relative"; // absolute, relative
        public boolean isMT;

        // public Date vpDate;
        public String fromMsisdn;
        public String toMsisdn;

        public String message;
        public String messageId;
        public String shortcode;

        public String getToMsisdn() {
            return toMsisdn;
        }

        public void setToMsisdn(String toMsisdn) {
            this.toMsisdn = toMsisdn;
        }

        public String getFromMsisdn() {
            return fromMsisdn;
        }

        public void setFromMsisdn(String fromMsisdn) {
            this.fromMsisdn = fromMsisdn;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "SMSMsg{" +
                    "isMT=" + isMT +
                    ", rsr='" + rsr + '\'' +
                    ", udType='" + udType + '\'' +
                    ", udEncoding='" + udEncoding + '\'' +
                    ", vpType='" + vpType + '\'' +
                    ", fromMsisdn='" + fromMsisdn + '\'' +
                    ", toMsisdn='" + toMsisdn + '\'' +
                    ", message='" + message + '\'' +
                    ", messageId='" + messageId + '\'' +
                    ", shortcode='" + shortcode + '\'' +
                    '}';
        }
    }


}