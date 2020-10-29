package in.wynk.sms.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import in.wynk.sms.config.SQSConfig;
import in.wynk.sms.model.SQSQueue;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static in.wynk.sms.constants.SmsMarkers.SQS_CONSUMER_ERROR;

/**
 * This class contains methods for the consumption as well as deletion of the messages from the SQS queues.
 * @author Abhishek
 * @created 24/09/19
 */
@Component
public class SQSMessageConsumer {

  @Autowired
  private SQSConfig sqsConfig;

  private static final Logger logger = LoggerFactory.getLogger(SQSMessageConsumer.class);

  /**
   * Returns the list of the messages which are consumed from the queue. The queue is selected according to the priority. The
   * maximum number of messages which can be consumed is 10.
   * @param smsPriority the priority according to which message is consumed from the corresponding queue.
   * @return the list of the messages consumed from the corresponding queue.
   */
  public List<Message> consumeMessage(String smsPriority) {
    SQSQueue SQSQueue = sqsConfig.getQueue(smsPriority);
    return getMessages(SQSQueue);
  }

  public List<Message> getMessages(SQSQueue sqsQueue, int maxNumberOfMessages, int waitTimeSeconds){
    AmazonSQS sqs = sqsConfig.getSqs();
    ReceiveMessageRequest receiveMessageRequest = null;
    List<Message> messages = null;
    try {
      receiveMessageRequest = new ReceiveMessageRequest(sqsQueue.getQueueUrl()).withMaxNumberOfMessages(maxNumberOfMessages).withWaitTimeSeconds(waitTimeSeconds);
      messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    } catch (Exception e){
      logger.error(SQS_CONSUMER_ERROR, "Unable to receive message for message request: {}", receiveMessageRequest, e);
    }
    if (CollectionUtils.isNotEmpty(messages))
      logger.debug("Consumed {} messages", messages.size());
    return messages;
  }

  public List<Message> getMessages(SQSQueue sqsQueue) {
    return getMessages(sqsQueue, 10, 1);
  }

  /**
   * Deletes the batch of messages from the sqs queue. The queue is selected on the basis of priority.
   * @param messages the batch of messages which needs to be deleted from the corresponding queue.
   * @param smsPriority the priority according to which queue is selected.
   */
  public void deleteMessages(List<Message> messages, String smsPriority) {
    SQSQueue sqsQueue = sqsConfig.getQueue(smsPriority);
    deleteMessages(messages, sqsQueue);
  }

  public void deleteMessages(List<Message> messages, SQSQueue sqsQueue){
    AmazonSQS sqs = sqsConfig.getSqs();
    final DeleteMessageBatchRequest batchRequest = new DeleteMessageBatchRequest().withQueueUrl(sqsQueue.getQueueUrl());
    final List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>();
    for (Message message : messages) {
      entries.add(new DeleteMessageBatchRequestEntry().withReceiptHandle(message.getReceiptHandle()).withId(message.getMessageId()));
    }
    batchRequest.setEntries(entries);
    try {
      DeleteMessageBatchResult deleteMessageBatchResult = sqs.deleteMessageBatch(batchRequest);
      List<DeleteMessageBatchResultEntry> successfullyDeleted = deleteMessageBatchResult.getSuccessful();
      logger.info("Deleted {} messages",successfullyDeleted.size());
      List<BatchResultErrorEntry> failedToDelete = deleteMessageBatchResult.getFailed();
      logger.info("Failed to delete {} messages",failedToDelete.size());
      //TODO : check if all the messages in the batch have been deleted
    } catch (Exception e){
      logger.error(SQS_CONSUMER_ERROR, "Unable to delete messages for request: {}", batchRequest, e);
    }
  }
}
