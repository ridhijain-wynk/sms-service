package in.wynk.sms.utils;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
public class WhatsappUtils {
    public static HttpHeaders getHMACAuthHeaders(String username, String secret) {
        final String algorithm = "hmac-sha256";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");
        final String currentDate = LocalDateTime.now(ZoneId.of("Etc/UTC")).format(formatter) + " GMT";
        final String signingString = "X-Date: " + currentDate;
        final byte[] signature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmac(signingString);
        final String signatureString = Base64.getEncoder().encodeToString(signature);
        final String authorization = "hmac username=\"" + username + "\", algorithm=\"" + algorithm + "\", headers=\"X-Date\", signature=\"" + signatureString + "\"";

        HttpHeaders requestHeaders = new org.springframework.http.HttpHeaders();
        requestHeaders.add("X-Date", currentDate);
        requestHeaders.add("Authorization", authorization);
        requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return requestHeaders;

        /*final Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("X-Date", currentDate);
        authHeaders.put("Authorization", authorization);
        return authHeaders;*/
    }
}
