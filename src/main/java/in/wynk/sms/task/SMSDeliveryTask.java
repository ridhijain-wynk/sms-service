
package in.wynk.sms.task;

import in.wynk.sms.model.SMSDto;
import in.wynk.sms.sender.SMSService;
import in.wynk.sms.util.SMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class SMSDeliveryTask implements Callable<String> {

    private static final Logger logger = LoggerFactory.getLogger(SMSDeliveryTask.class);

    private String smsToBeDelivered;
    private SMSService smsService;

    public SMSDeliveryTask(String smsToBeDelivered, SMSService smsService) {
        this.smsService = smsService;
        this.smsToBeDelivered = smsToBeDelivered;
    }

    public String call() throws Exception {
        deliverMessages();
        return null;
    }

    private void deliverMessages() {
        SMSDto smsObj = SMSUtils.getSMSObjectFromJSONString(smsToBeDelivered);
        if (smsObj != null) {
            String time = String.valueOf(System.currentTimeMillis() - smsObj.getCreationTimestamp());
            logger.info("Dequeued SMS. SMS id:" + smsObj.getId() + " : SMS Priority: " + smsObj.getPriority() + " : Total time taking in dequeing sms:" + time + " ms");

            int retryCount = 3;
            boolean deliverStatus = false;
            while (retryCount-- > 0 && !deliverStatus) {
                try {
                    smsService.sendMessage(smsObj);
                    deliverStatus=true;
                } catch (Exception e) {
                    logger.error("Error while executing SMSDeliveryTask.deliverMessages", e);
                }
            }
        } else {
            logger.error("Error while parsing redis sms string to object");
        }
    }

}
