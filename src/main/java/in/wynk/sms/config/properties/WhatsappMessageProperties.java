package in.wynk.sms.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "wynk.iq.whatsapp")
public class WhatsappMessageProperties {
    private boolean enabled;
    private Map<String, String> urls;
}
