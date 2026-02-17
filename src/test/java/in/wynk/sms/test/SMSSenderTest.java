package in.wynk.sms.test;

import in.wynk.http.config.HttpClientConfig;
import in.wynk.sms.SMSApplication;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.sender.AirtelSMSSender;
import in.wynk.sms.sender.IQAirtelSMSSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SMSApplication.class, HttpClientConfig.class})
public class SMSSenderTest {

    @Autowired
    private AirtelSMSSender airtelSMSSender;

    @Autowired
    private IQAirtelSMSSender iqAirtelSMSSender;

    @Test
    public void testMusicSMS() throws Exception {
        SmsRequest request = SmsTestUtils.lowPrioritySms("9911442662", "music");
        airtelSMSSender.sendMessage(request);
    }

    @Test
    public void testBunkerFitSMS() throws Exception {
        SmsRequest request = SmsTestUtils.lowPrioritySms("9911442662", null, "bunkerfit");
        airtelSMSSender.sendMessage(request);
    }

    @Test
    public void testBooksSMS() throws Exception {
        SmsRequest request = SmsTestUtils.lowPrioritySms("9911442662", null, "AIRTEL_BOOKS");
        airtelSMSSender.sendMessage(request);
    }

    @Test
    public void testIQMessage() throws Exception {
        SmsRequest request = SmsTestUtils.highPrioritySms("9650127579", null);
        iqAirtelSMSSender.sendMessage(request);
        SmsRequest request2 = SmsTestUtils.highPrioritySms2("9650127579", null);
        iqAirtelSMSSender.sendMessage(request2);

    }
}
