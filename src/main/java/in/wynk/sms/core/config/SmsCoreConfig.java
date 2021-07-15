package in.wynk.sms.core.config;

import in.wynk.data.config.WynkMongoDbFactoryBuilder;
import in.wynk.data.config.properties.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "in.wynk.sms.core.repository", mongoTemplateRef = "smsMongoTemplate")
public class SmsCoreConfig {

    public MongoDbFactory smsDbFactory(MongoProperties mongoProperties) {
        return WynkMongoDbFactoryBuilder.buildMongoDbFactory(mongoProperties, "sms");
    }

    @Bean("smsMongoTemplate")
    @Primary
    public MongoTemplate smsMongoTemplate(MongoProperties mongoProperties) {
        return new MongoTemplate(smsDbFactory(mongoProperties));
    }

}