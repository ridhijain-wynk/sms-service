package in.wynk.sms.kafka;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.rate.limiter.advice.RateLimiter;
import in.wynk.rate.limiter.constant.RateLimiterConstant;
import in.wynk.rate.limiter.exception.WynkRateLimitException;
import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.common.dto.wa.outbound.WhatsappMessageRequest;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.stream.constant.StreamMarker;
import in.wynk.stream.consumer.impl.AbstractKafkaEventConsumer;
import in.wynk.stream.producer.IKafkaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.*;
import static in.wynk.common.constant.BaseConstants.IN_MEMORY_CACHE_CRON;
import static in.wynk.sms.common.constant.SMSPriority.HIGHEST;

@Slf4j
@Service
@DependsOn("kafkaConsumerConfig")
public class WhatsappKafkaConsumer extends AbstractKafkaEventConsumer<String, WhatsappMessageRequest> {
    private final IWhatsappKafkaHandler<WhatsappMessageRequest> whatsappKafkaHandler;
    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final SendersCachingService sendersCachingService;
    private final IKafkaEventPublisher<String, AbstractWhatsappOutboundMessage> kafkaEventPublisher;
    private final ClientDetailsCachingService clientDetailsCachingService;
    @Value("${wynk.kafka.consumers.enabled}")
    private boolean enabled;
    @Value("${wynk.kafka.producers.whatsapp.iq.retry.message.topic}")
    private String kafkaRetryTopic;
    @Value("${wynk.kafka.producers.whatsapp.iq.dlt.message.topic}")
    private String kafkaDLTTopic;
    private final ScheduledExecutorService scheduler;

    public WhatsappKafkaConsumer (IWhatsappKafkaHandler<WhatsappMessageRequest> whatsappKafkaHandler, SendersCachingService sendersCachingService, KafkaListenerEndpointRegistry endpointRegistry,
                                  IKafkaEventPublisher<String, AbstractWhatsappOutboundMessage> kafkaEventPublisher, ClientDetailsCachingService clientDetailsCachingService) {
        super();
        this.whatsappKafkaHandler = whatsappKafkaHandler;
        this.sendersCachingService = sendersCachingService;
        this.endpointRegistry = endpointRegistry;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.clientDetailsCachingService = clientDetailsCachingService;
        this.scheduler = Executors.newScheduledThreadPool(3);
    }

    @Override
    public void consume(WhatsappMessageRequest request) {
        String timeInterval = RateLimiterConstant.DEFAULT_TIME_INTERVAL;
        String maxCalls = RateLimiterConstant.DEFAULT_MAX_CALLS;
        final Senders senders = sendersCachingService.getSenderByNameClientCountry(SMSConstants.WHATSAPP_SENDER_BEAN, request.getClientAlias(), HIGHEST, SMSConstants.DEFAULT_COUNTRY_CODE);
        if(Objects.nonNull(senders) && Objects.nonNull(senders.getRateLimit()) &&
                Objects.nonNull(senders.getRateLimit().getTimeWindowInSeconds()) &&
                Objects.nonNull(senders.getRateLimit().getMaxCalls())){
            timeInterval = senders.getRateLimit().getTimeWindowInSeconds();
            maxCalls = senders.getRateLimit().getMaxCalls();
        }
        sendMessage(request, timeInterval, maxCalls);
    }

    @RateLimiter(key = "#request.getClientAlias()", value = "#request.getMessage().getType()", interval = "#timeInterval", maxCalls = "#maxCalls")
    private void sendMessage(WhatsappMessageRequest request, String timeInterval, String maxCalls){
        AnalyticService.update(SMSConstants.TIME_INTERVAL, timeInterval);
        AnalyticService.update(SMSConstants.CALLS_PERMITTED, maxCalls);
        whatsappKafkaHandler.sendMessage(request);
    }

    /*@RetryableTopic(
            attempts = "#{'${wynk.kafka.consumers.listenerFactory.whatsapp[0].retry.maxRetryAttempts}'}",
            autoCreateTopics = "#{'${wynk.kafka.consumers.listenerFactory.whatsapp[0].retry.autoCreateRetryTopics}'}",
            backoff = @Backoff(delayExpression = "#{'${wynk.kafka.consumers.listenerFactory.whatsapp[0].retry.retryIntervalMilliseconds}'}", multiplierExpression = "#{'${wynk.kafka.consumers.listenerFactory.whatsapp[0].retry.retryBackoffMultiplier}'}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.MULTIPLE_TOPICS,
            include = {WynkRuntimeException.class},
            exclude = {WynkRateLimitException.class},
            timeout = "#{'${wynk.kafka.consumers.listenerFactory.whatsapp[0].retry.maxRetryDurationMilliseconds}'}",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)*/
    @KafkaListener(id = "whatsappSendMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.whatsapp[0].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.whatsapp[0].name}")
    @AnalyseTransaction(name = "whatsappSendMessage")
    protected void listenWhatsappSendMessage(@Header(BaseConstants.REQUEST_ID) String requestId, @Header(BaseConstants.ORG_ID) String orgId, @Header(BaseConstants.SERVICE_ID) String service, ConsumerRecord<String, AbstractWhatsappOutboundMessage> consumerRecord) {
        try {
            if(Objects.nonNull(consumerRecord.value())){
                final String clientAlias = clientDetailsCachingService.getClientByService(service).getAlias();
                final WhatsappMessageRequest request = WhatsappMessageRequest.builder()
                        .message(consumerRecord.value())
                        .clientAlias(clientAlias)
                        .requestId(requestId)
                        .serviceId(service)
                        .orgId(orgId)
                        .build();
                AnalyticService.update(request);
                consume(request);
            }
        } catch (Exception e) {
            if(WynkRuntimeException.class.isAssignableFrom(e.getClass()) || WynkRateLimitException.class.isAssignableFrom(e.getClass())){
                log.error(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED_NO_RETRY, e.getMessage(), e);
                return;
            }
            log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Error occurred in polling/consuming kafka event", e);
            scheduleRetry(kafkaRetryTopic, service, orgId, requestId, consumerRecord, "0");
        }
    }

    private void scheduleRetry(String topic, String service, String orgId, String requestId, ConsumerRecord<String, AbstractWhatsappOutboundMessage> consumerRecord, String retryAttempt) {
        // Schedule a retry with backoff delay
        scheduler.schedule(() -> {
            // Send the failed message to the retry-topic
            publishEventInKafka(topic, service, orgId, requestId, consumerRecord, retryAttempt);
        }, IN_MEMORY_CACHE_CRON, TimeUnit.MILLISECONDS);
    }

    private void publishEventInKafka (String topic, String service, String orgId, String requestId, ConsumerRecord<String, AbstractWhatsappOutboundMessage> consumerRecord, String retryAttempt){
        try{
            final RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader(BaseConstants.SERVICE_ID, service.getBytes()));
            headers.add(new RecordHeader(BaseConstants.ORG_ID, orgId.getBytes()));
            headers.add(new RecordHeader(BaseConstants.REQUEST_ID, requestId.getBytes()));
            headers.add(new RecordHeader(SMSConstants.KAFKA_RETRY_COUNT, retryAttempt.getBytes()));
            kafkaEventPublisher.publish(topic, null, System.currentTimeMillis(), UUIDs.timeBased().toString(),
                    consumerRecord.value(),
                    headers);
        } catch(Exception ignored){}
    }

    @KafkaListener(id = "whatsappRetryMessageListener", topics = "${wynk.kafka.consumers.listenerFactory.whatsapp[1].factoryDetails.topic}", containerFactory = "${wynk.kafka.consumers.listenerFactory.whatsapp[1].name}")
    @AnalyseTransaction(name = "whatsappRetryMessage")
    protected void listenWhatsappRetryMessage(@Header(BaseConstants.REQUEST_ID) String requestId, @Header(BaseConstants.ORG_ID) String orgId, @Header(BaseConstants.SERVICE_ID) String service, @Header(SMSConstants.KAFKA_RETRY_COUNT) String retryCount, ConsumerRecord<String, AbstractWhatsappOutboundMessage> consumerRecord) {
        try {
            if(Objects.nonNull(consumerRecord.value()) && !StringUtils.isEmpty(retryCount)){
                final String clientAlias = clientDetailsCachingService.getClientByService(service).getAlias();
                int retryAttempt = Integer.parseInt(retryCount);
                if(retryAttempt < 3){
                    final WhatsappMessageRequest request = WhatsappMessageRequest.builder()
                            .message(consumerRecord.value())
                            .clientAlias(clientAlias)
                            .requestId(requestId)
                            .serviceId(service)
                            .orgId(orgId)
                            .build();
                    consume(request);
                } else {
                    publishEventInKafka(kafkaDLTTopic, service, orgId, requestId, consumerRecord, String.valueOf(Integer.parseInt(retryCount) + 1));
                    log.info(StreamMarker.KAFKA_RETRY_EXHAUSTION_ERROR, "Event from topic is dead lettered - event:" + consumerRecord.value());
                }
            }
        } catch (Exception e) {
            if(e.getClass().isAssignableFrom(WynkRuntimeException.class) || e.getClass().isAssignableFrom(WynkRateLimitException.class)){
                log.info(SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED_NO_RETRY, "Rate limit exceeded in polling kafka event, no retry", e);
                return;
            }
            log.error(StreamMarker.KAFKA_POLLING_CONSUMPTION_ERROR, "Error occurred in polling/consuming kafka event", e);
            scheduleRetry(kafkaRetryTopic, service, orgId, requestId, consumerRecord, String.valueOf(Integer.parseInt(retryCount) + 1));
        }
    }

    @Override
    public void start() {
        if (enabled) {
            log.info("Starting Kafka consumption...");
        }
    }

    @Override
    public void stop() {
        if (enabled) {
            log.info("Shutting down Kafka consumption...");
            this.endpointRegistry.stop();
        }
    }
}