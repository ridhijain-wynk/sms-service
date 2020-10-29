package in.wynk.sms.processor;


import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.SendSmsResponse;
import in.wynk.sms.model.SmsControl;
import in.wynk.sms.model.enums.SMSStatus;
import in.wynk.sms.producer.SQSMessageProducer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Handles the input Incoming sms sending request by maintaining input worker queue.
 *
 * @author prerna
 */
@Component
public class SMSEnqueueProcessor {

    @Autowired
    private SQSMessageProducer sqsMessageProducer;

    private static final Logger logger = LoggerFactory.getLogger(SMSEnqueueProcessor.class);

    private static final int RETRY_WORKER = 3;

	/*
	@Scheduled(initialDelay=0, fixedDelay=1000*60*10)
	public void updateOutputWorkersStatus() {
		String status = priorityQueueManager.get(SMSConstants.REDIS_SMS_INPUT_WORKERS);
		if (status != null) {
			this.status = status;
		}
	}
*/

    /**
     * Incoming messages are added to PQ by worker threads.
     * PQ is then processed by output workers to deliver messages based on priority.
     * <p>
     * Input processor's job is to enqueue the processor
     *
     * @param sms
     */
    public SendSmsResponse processSMSSendRequest(SMSDto sms) {
        SendSmsResponse response = new SendSmsResponse(null, SMSStatus.QUEUING_FAILED);
        try {
            boolean enqueueStatus = enqueueMessageToQueue(sms);
            if (enqueueStatus) {
                response = new SendSmsResponse(sms, SMSStatus.QUEUED);
            }
        } catch (Exception e) {
            logger.error("Error while executing processSMSSendRequest", e);
        }
        return response;
    }

    /**
     * Filters the incoming sms requests according to fixed conditions to control the traffic.
     * <p>
     * The incoming sms can be filtered according to following criteria:
     * <li>If the sms of a particular priority is not allowed</li>
     * <li>If the sms from a particular source is not allowed </li>
     * <li>If the sms matches with the message body which is not allowed</li>
     * <li>If the sms from a particular source and of a particular priority is not allowed</li>
     * </p>
     *
     * @param sms        the incoming message which needs to be ignored or sent in the queue.
     * @param smsControl the parameters to decide whether to push the message in the queue or ignore the message.
     * @return the {@link SendSmsResponse} with the corresponding status.
     */
    public SendSmsResponse filterAndProcessSMSSendRequest(SMSDto sms, SmsControl smsControl) {
        boolean allow = true;
        if (CollectionUtils.isNotEmpty(smsControl.getPriorities()) && smsControl.getPriorities().contains(sms.getPriority())) {
            //Blocks the message of a particular priority
            allow = false;
        } else if (CollectionUtils.isNotEmpty(smsControl.getSources()) && smsControl.getSources().contains(sms.getSource())) {
            //Blocks the message from a particular source
            allow = false;
        } else if (CollectionUtils.isNotEmpty(smsControl.getMessages())) {
            //Blocks the message if it starts from the specified prefix
            for (String msg : smsControl.getMessages()) {
                if (sms.getMessage().startsWith(msg)) {
                    allow = false;
                    break;
                }
            }
        } else if (!isEmpty(smsControl.getPrioritiesWithSource())) {
            //Blocks the message if it if of a particular priority from a particular source
            Map<String, Set<String>> prioritiesWithSources = smsControl.getPrioritiesWithSource();
            for (String source : prioritiesWithSources.keySet()) {
                if (sms.getSource().equalsIgnoreCase(source)) {
                    Set<String> priorities = prioritiesWithSources.get(source);
                    if (priorities.contains(sms.getPriority())) {
                        allow = false;
                        break;
                    }
                }
            }
        }
        if (allow) {
            return processSMSSendRequest(sms);
        } else {
            return generateDummyResponse(sms);
        }
    }

    /**
     * Generates a dummy {@link SendSmsResponse} response with {@link SMSStatus} status IGNORED.
     *
     * @param sms the sms which is ignored.
     * @return the response with status IGNORED.
     */
    private SendSmsResponse generateDummyResponse(SMSDto sms) {
        return new SendSmsResponse(sms, SMSStatus.IGNORED);
    }

    private boolean enqueueMessageToQueue(SMSDto sms) {
        int retryCount = RETRY_WORKER;
        boolean enqueueStatus = false;
        while (retryCount-- > 0 && !enqueueStatus) {
            try {
                //inputWorkers.submit(new SMSEnqueueTask(sms, priorityQueueManager));
//				pushToRedis(sms);
                pushInQueue(sms);
                enqueueStatus = true;
            } catch (Exception e) {
                logger.error("Error while submiting SMSEnqueueTask for sms:" + sms.toString());
            }
        }
        //logger.info(getInputWorkerStats());
        return enqueueStatus;
    }

    /**
     * Pushes the sms into the queue according to the priority.
     *
     * @param sms the sms need to be sent to the destination.
     */
    private void pushInQueue(SMSDto sms) {
        try {
            sqsMessageProducer.produceMessage(sms);
            logger.info("Queued SMS - SMS id:" + sms.getId() + ", SMS Priority: " + sms.getPriority()
                    + ", Total time taken in queuing:" + (System.currentTimeMillis() - sms.getCreationTimestamp()) + " ms");
        } catch (Exception e) {
            AnalyticService.update("queueEnqueueError", e.toString());
            logger.error("Error while enqueing message for sms:" + sms.toString(), e);
            throw e;
        }
    }

}
