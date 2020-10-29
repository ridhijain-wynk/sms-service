package in.wynk.sms.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import in.wynk.sms.config.SQSConfig;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.SQSQueue;
import in.wynk.sms.model.enums.SMSPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static in.wynk.sms.constants.SmsMarkers.SQS_PRODUCER_ERROR;

/**
 * This class contains the methods to insert sms into the SQS queues.
 * @author Abhishek
 * @created 24/09/19
 */
@Service
public class SQSMessageProducer {

  @Autowired
  private SQSConfig sqsConfig;

  private static final Logger logger = LoggerFactory.getLogger(SQSMessageProducer.class);

  /**
   * Inserts sms into the corresponding queue according to the priority of the sms.
   * @param sms the sms{@link SMSDto} which need to be pushed into the corresponding queue.
   */
  public void produceMessage(SMSDto sms) {
    SQSQueue SQSQueue = sqsConfig.getQueue(sms.getPriority());
    AmazonSQS sqs = sqsConfig.getSqs();
    final SendMessageRequest sendMessageRequest = new SendMessageRequest()
        .withMessageBody(sms.toJson()).withDelaySeconds(SMSPriority.delay(sms.getPriority()))
        .withQueueUrl(SQSQueue.getQueueUrl());//.withMessageDeduplicationId(sms.getService()+ sms.getMsisdn());
    logger.info("send message request: {}", sendMessageRequest.toString());
    try {
      SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
      logger.info("send message result: {}", sendMessageResult);
    } catch (Exception e) {
      logger.error(SQS_PRODUCER_ERROR, "Unable to send message request {} to queue ",sendMessageRequest, e);
      //TODO : Need to push this message in delay queue
      throw e;
    }
  }
}
