package in.wynk.sms.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "whatsapp.airtel.iq")
public class IQWhatsappProperties {

    private Consumer consumer;
    private Credentials credentials;
    private Map<String, String> endpoints;

    @Getter
    @Setter
    public static class Credentials {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String username;
    }
}