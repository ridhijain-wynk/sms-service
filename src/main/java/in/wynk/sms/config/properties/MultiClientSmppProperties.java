package in.wynk.sms.config.properties;


import in.wynk.smpp.config.properties.SmppProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "wynk.saas.smpp")
public class MultiClientSmppProperties {

    private boolean enabled;
    private Map<String, SmppProperties> clients;
    private boolean setupRightAway = true;

}
