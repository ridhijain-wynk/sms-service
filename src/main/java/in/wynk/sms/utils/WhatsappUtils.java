package in.wynk.sms.utils;

import com.github.annotation.analytic.core.service.AnalyticService;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

public class WhatsappUtils {
    public static HttpHeaders getHMACAuthHeaders(String username, String secret) {
        final String algorithm = "hmac-sha256";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");
        final String currentDate = LocalDateTime.now(ZoneId.of("Etc/UTC")).format(formatter) + " GMT";
        final String signingString = "X-Date: " + currentDate;
        final byte[] signature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmac(signingString);
        final String signatureString = Base64.getEncoder().encodeToString(signature);
        final String authorization = "hmac username=\"" + username + "\", algorithm=\"" + algorithm + "\", headers=\"X-Date\", signature=\"" + signatureString + "\"";
        final String correlationID = UUID.randomUUID().toString();
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-Date", currentDate);
        requestHeaders.add("Authorization", authorization);
        requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("X-Correlation-ID", correlationID);
        AnalyticService.update(correlationID);
        return requestHeaders;
    }

    public static HttpHeaders getBasicAuthHeaders(String username, String secret) {
        final String credentials = username + ":" + secret;
        final String authorization = Base64Utils.encodeToString(credentials.getBytes());
        final String correlationID = UUID.randomUUID().toString();
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Basic " + authorization);
        requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("X-Correlation-ID", correlationID);
        AnalyticService.update(correlationID);
        return requestHeaders;
    }
}
