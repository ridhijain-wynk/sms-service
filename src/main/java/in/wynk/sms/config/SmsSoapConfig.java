package in.wynk.sms.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsSoapConfig {

    @Bean
    public XmlMapper marshaller() {
        return new XmlMapper();
    }

}
