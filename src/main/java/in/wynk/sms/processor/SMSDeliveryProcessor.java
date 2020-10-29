package in.wynk.sms.processor;

import com.amazonaws.services.sqs.model.Message;
import in.wynk.sms.config.SQSConfig;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.consumer.SQSMessageConsumer;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.enums.SMSPriority;
import in.wynk.sms.sender.SMSService;
import in.wynk.sms.task.SMSDeliveryTask;
import in.wynk.sms.util.SMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static in.wynk.sms.model.enums.SMSPriority.LOW;
import static in.wynk.sms.model.enums.SMSPriority.MEDIUM;


/**
 * Handles all the SMS Delivery related operations
 *
 * @author prerna
 */
@Configuration
@Component
public class SMSDeliveryProcessor {

    @Autowired
    private SQSMessageConsumer sqsMessageConsumer;

    @Value("${high.priority.batch.count}")
    private int threadPool;

    @Autowired
    private SQSConfig sqsConfig;

    private ThreadPoolExecutor threadPoolExecutor;


    @Autowired
    private SMSService smsService;


    private static final Logger logger = LoggerFactory.getLogger(SMSDeliveryProcessor.class);

    @PostConstruct
    public void initialize() {
        checkAndDeliver();
        threadPoolExecutor = new ThreadPoolExecutor(threadPool, threadPool, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }


    private void checkAndDeliver() {
        Thread highPriorityThread = new Thread(new Processor(SMSPriority.HIGH));
        highPriorityThread.setName("pull_message_thread_high_priority");
        highPriorityThread.setDaemon(true);
        highPriorityThread.start();

        Thread mediumPriorityThread = new Thread(new Processor(MEDIUM));
        mediumPriorityThread.setName("pull_message_thread_medium_priority");
        mediumPriorityThread.setDaemon(true);
        mediumPriorityThread.start();

        Thread lowPriorityThread = new Thread(new Processor(LOW));
        lowPriorityThread.setName("pull_message_thread_low_priority");
        lowPriorityThread.setDaemon(true);
        lowPriorityThread.start();
    }

    private Set<String> getMessagesToBeDelivered(SMSPriority priority) {
        Set<String> smsToBeDelivered = new HashSet<>();
        List<Message> messageList = sqsMessageConsumer.consumeMessage(priority.name());
        List<Message> messagesToBeDeleted = new ArrayList<>();
        for (Message message : messageList) {
            SMSDto sms = SMSUtils.getSMSObjectFromJSONString(message.getBody());
            if (sms != null) {
                if (sms.isNineToNine() && !SMSUtils.isvalidSMSDeliveryHour()) {
                    //we will not pick these messages
                    logger.info("sms: {} is not allowed during this time", sms);
                } else {
                    smsToBeDelivered.add(sms.toString());
                    messagesToBeDeleted.add(message);
                }
            } else {
                messagesToBeDeleted.add(message);
                logger.info("faulty message {} ", message);
            }
            if (!CollectionUtils.isEmpty(messagesToBeDeleted)) {
                sqsMessageConsumer.deleteMessages(messagesToBeDeleted, priority.name());
            }
        }
        return smsToBeDelivered;
    }

    private boolean checkIfNineToNine() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 21;
    }


    private class Processor implements Runnable {

        private SMSPriority priority;

        Processor(SMSPriority priority) {
            this.priority = priority;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Set<String> smsToBeDelivered = getMessagesToBeDelivered(priority);
                    if (smsToBeDelivered.size() > 0) {
                        logger.info("Messages Dequed of size " + smsToBeDelivered.size() + " of priority " + priority);
                        for (String sms : smsToBeDelivered) {
                            threadPoolExecutor.submit(new SMSDeliveryTask(sms, smsService));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while submitting task to " + SMSConstants.REDIS_QUEUE_KEY + "_" + priority + " " + e);
                }
            }
        }
    }
}