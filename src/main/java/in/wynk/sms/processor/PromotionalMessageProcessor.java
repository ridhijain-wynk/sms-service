package in.wynk.sms.processor;

import com.amazonaws.services.sqs.model.Message;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import in.wynk.sms.config.SQSConfig;
import in.wynk.sms.constants.SmsMarkers;
import in.wynk.sms.consumer.SQSMessageConsumer;
import in.wynk.sms.model.SMSDto;
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
 * @created 24/06/20
 */
@Component
public class PromotionalMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PromotionalMessageProcessor.class);

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

    private void consumeAndProduce(){
        while (true) {
            try {
                List<Message> promotionalMessages = sqsMessageConsumer.getMessages(sqsConfig.getPromotionalMessageQueue(), 10, 10);
                if(CollectionUtils.isNotEmpty(promotionalMessages)) {
                    for (Message message : promotionalMessages) {
                        List<SendSmsRequest> jsonArray = gson.fromJson(message.getBody(), new TypeToken<List<SendSmsRequest>>() {
                        }.getType());
                        for (SendSmsRequest requestJson : jsonArray) {
                            if (requestJson != null) {
                                SMSDto dto = parseMessage(requestJson);
                                sqsMessageProducer.produceMessage(dto);
                            }
                        }
                    }
                    sqsMessageConsumer.deleteMessages(promotionalMessages, sqsConfig.getPromotionalMessageQueue());
                }
            }catch (Exception e){
                logger.error(SmsMarkers.PROMOTIONAL_MSG_ERROR, "Unable to process promotional messages", e);
            }
        }
    }


    @AnalyseTransaction(name = "consumePromotionalMessage")
    private SMSDto parseMessage(SendSmsRequest request) {
        AnalyticService.update("source", request.getSource());
        AnalyticService.update("msisdn", request.getMsisdn());
        AnalyticService.update("message", request.getMessage());
        AnalyticService.update("priority", request.getPriority());
        return smsFactory.getSMSDto(request);
    }
}
