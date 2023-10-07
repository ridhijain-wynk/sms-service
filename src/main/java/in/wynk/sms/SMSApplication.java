package in.wynk.sms;

import in.wynk.client.core.config.ClientCoreConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication(scanBasePackages = "in.wynk",exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, ClientCoreConfig.class})
public class SMSApplication implements ApplicationRunner {


    public static void main(String[] args) {
        SpringApplication.run(SMSApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("SMSApplication Starting");
    }
}


