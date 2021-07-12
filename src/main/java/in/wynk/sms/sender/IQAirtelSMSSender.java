package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import in.wynk.sms.dto.request.SmsRequest;
import org.springframework.stereotype.Component;

import static in.wynk.sms.constants.SMSConstants.AIRTEL_IQ_SMS_SENDER_BEAN;

@Component(AIRTEL_IQ_SMS_SENDER_BEAN)
public class IQAirtelSMSSender extends AbstractSMSSender{

    @Override
    @AnalyseTransaction(name = "sendSmsAirtelIQ")
    public void sendMessage(SmsRequest request) throws Exception {

    }
}
