package in.wynk.sms.core.config;

import com.amazonaws.services.pinpoint.AmazonPinpoint;
import com.amazonaws.services.pinpoint.AmazonPinpointClientBuilder;
import in.wynk.aws.common.properties.AmazonSdkProperties;
import in.wynk.data.config.WynkMongoDbFactoryBuilder;
import in.wynk.data.config.properties.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "in.wynk.sms.core.repository", mongoTemplateRef = "smsMongoTemplate")
public class SmsCoreConfig {

    public MongoDatabaseFactory smsDbFactory(MongoProperties mongoProperties) {
        return WynkMongoDbFactoryBuilder.buildMongoDbFactory(mongoProperties, "sms");
    }

    @Bean("smsMongoTemplate")
    @Primary
    public MongoTemplate smsMongoTemplate(MongoProperties mongoProperties) {
        return new MongoTemplate(smsDbFactory(mongoProperties));
    }

    @Bean
    public AmazonPinpoint amazonPinpoint(AmazonSdkProperties sdkProperties) {
        return AmazonPinpointClientBuilder.standard().withRegion(sdkProperties.getSdk().getRegions()).build();
    }

}