package in.wynk.sms.sender;

import in.wynk.sms.dto.request.SmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSMSSender implements IMessageSender<SmsRequest> {

	protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

	public abstract void sendMessage(SmsRequest request) throws Exception;

}
