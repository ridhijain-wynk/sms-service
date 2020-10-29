package in.wynk.sms;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "in.wynk")
public class SMSApplication implements ApplicationRunner {


    public static void main(String[] args) {
        SpringApplication.run(SMSApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("SMSApplication Starting");
    }
}


