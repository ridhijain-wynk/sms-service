package in.wynk.sms.processor;

import com.amazonaws.services.sqs.model.Message;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import com.google.gson.Gson;
import in.wynk.sms.config.SQSConfig;
import in.wynk.sms.consumer.SQSMessageConsumer;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.SQSQueue;
import in.wynk.sms.model.SendSmsRequest;
import in.wynk.sms.producer.SQSMessageProducer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Abhishek
 * @created 17/07/20
 */
@Component
public class NotificationMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationMessageProcessor.class);

    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SQSMessageConsumer sqsMessageConsumer;

    @Autowired
    private SQSMessageProducer sqsMessageProducer;

    @Autowired
    private SQSConfig sqsConfig;

    @Autowired
    private SMSFactory smsFactory;

    @Autowired
    private Gson gson;

    @PostConstruct
    public void initialize() {
        threadPoolExecutor = new ThreadPoolExecutor(2, 4, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        threadPoolExecutor.execute(this::consumeAndProduce);
        threadPoolExecutor.execute(this::consumeAndProduce);
    }

    private void consumeAndProduce() {
        while (true) {
            try {
                SQSQueue notificationMessageQueue = sqsConfig.getNotificationMessageQueue();
                List<Message> notificationMessages = sqsMessageConsumer.getMessages(notificationMessageQueue);
                if (CollectionUtils.isNotEmpty(notificationMessages)) {
                    for (Message message : notificationMessages) {
                        SendSmsRequest requestJson = gson.fromJson(message.getBody(), SendSmsRequest.class);
                        if (requestJson != null) {
                            SMSDto dto = parseMessage(requestJson);
                            logger.debug("sms: {}", dto);
                            sqsMessageProducer.produceMessage(dto);
                        }
                    }
                    sqsMessageConsumer.deleteMessages(notificationMessages, notificationMessageQueue);
                }
            } catch (Exception e) {
                logger.error("Unable to process notification messages", e);
            }
        }
    }

    @AnalyseTransaction(name = "consumeNotificationMessage")
    private SMSDto parseMessage(SendSmsRequest request) {
        AnalyticService.update("source", request.getSource());
        AnalyticService.update("msisdn", request.getMsisdn());
        AnalyticService.update("message", request.getMessage());
        AnalyticService.update("priority", request.getPriority());
        return smsFactory.getSMSDto(request);
    }
}