package in.wynk.sms.listener;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.queue.service.ISqsManagerService;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.dto.wa.inbound.OrderDetailsRespEvent;
import in.wynk.sms.common.message.SmsNotificationMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.entity.SenderConfigurations;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.IRedisCacheService;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.dto.SMSFactory;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.event.ClientPinpointStreamEvent;
import in.wynk.sms.event.IQDeliveryReportEvent;
import in.wynk.sms.event.SmsNotificationEvent;
import in.wynk.sms.event.WhatsappOrderDetailsEvent;
import in.wynk.sms.sender.IMessageSender;
import in.wynk.spel.IRuleEvaluator;
import in.wynk.spel.builder.DefaultStandardExpressionContextBuilder;
import in.wynk.stream.producer.IKafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static in.wynk.common.constant.BaseConstants.*;
import static in.wynk.sms.constants.SMSConstants.AIRTEL_IQ_SMS_SENDER_BEAN;
import static in.wynk.sms.constants.SMSConstants.PINPOINT_SENDER_BEAN;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsEventsListener {

    @Value("${wynk.kafka.producers.whatsapp.iq.inbound.topic}")
    private String whatsappInboundTopic;
    private final ObjectMapper objectMapper;
    private final IRuleEvaluator ruleEvaluator;
    private final ISqsManagerService sqsManagerService;
    private final MessageCachingService messageCachingService;
    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SenderConfigurationsCachingService senderConfigCachingService;
    private final IRedisCacheService redisDataService;
    private final IKafkaEventPublisher<String, String> kafkaEventPublisher;

    @EventListener
    @AnalyseTransaction(name = "smsNotificationEvent")
    public void onSmsNotificationEvent(SmsNotificationEvent event) {
        if (StringUtils.isNotEmpty(event.getMsisdn())) {
            if (StringUtils.isNotEmpty(event.getMessage())) {
                log.info(OLD_MESSAGE_PATTERN, "Resolved message present for {}", event.getMsisdn());
                SmsRequest smsRequest = SMSFactory.getSmsRequest(SmsNotificationMessage.builder()
                        .message(event.getMessage())
                        .msisdn(event.getMsisdn())
                        .service(event.getService())
                        .priority(event.getPriority())
                        .messageId(event.getMessageId())
                        .build());
                AnalyticService.update(smsRequest);
                sqsManagerService.publishSQSMessage(smsRequest);
            } else if (Objects.nonNull(event.getContextMap())) {
                final String circleCode = String.valueOf(event.getContextMap().get(CIRCLE_CODE));
                Messages message = getMessage(event.getMessageId(), circleCode);

                if (Objects.isNull(message)) {
                    log.error(MESSAGE_NOT_FOUND, "Unable to find linked message {} ", event.getMessageId());
                    return;
                }
                AnalyticService.update(CIRCLE_CODE, circleCode);
                AnalyticService.update(IS_ENABLED, message.isEnabled());
                AnalyticService.update(MESSAGE_TEMPLATE_TAGS, message.getTags());
                AnalyticService.update(INTERNAL_MESSAGE_TEMPLATE_ID, message.getId());
                final String smsMessage = message.getMessage();
                if (message.isEnabled()) {
                    final StandardEvaluationContext seContext = DefaultStandardExpressionContextBuilder.builder()
                            .variable(CONTEXT_MAP, event.getContextMap())
                            .build();
                    final String evaluatedMessage = ruleEvaluator.evaluate(smsMessage, () -> seContext, SMS_MESSAGE_TEMPLATE_CONTEXT, String.class);

                    SmsRequest smsRequest = SMSFactory.getSmsRequest(SmsNotificationMessage.builder()
                            .message(evaluatedMessage)
                            .msisdn(event.getMsisdn())
                            .service(event.getService())
                            .priority(message.getPriority())
                            .messageId(message.getId())
                            .build());
                    AnalyticService.update(smsRequest);
                    sqsManagerService.publishSQSMessage(smsRequest);
                    log.info("Message pushed for request for " + smsRequest.getMessageId() + "- " + smsRequest.getMsisdn());
                }
            }
        }
    }

    @EventListener
<<<<<<< HEAD
    @AnalyseTransaction(name = "IQDeliveryReportEvent")
    public void onIQDeliveryReportEvent(IQDeliveryReportEvent event) {
        AnalyticService.update(event);
        final SmsRequest smsRequest = redisDataService.get(event.getMessageRequestId());
        if (Objects.nonNull(smsRequest)) {
            AnalyticService.update(smsRequest);
            sendThroughFallback(smsRequest, AIRTEL_IQ_SMS_SENDER_BEAN);
        } else {
            log.info("Message request not found in redis.");
        }
    }

    @EventListener
=======
>>>>>>> aef351d93e7e711aba50dd12ac02b402dbee9ca6
    @AnalyseTransaction(name = "pinpointStreamEvent")
    public void onPinpointSMSEvent(ClientPinpointStreamEvent event) {
        AnalyticService.update(event);
        if (StringUtils.equalsIgnoreCase(event.getPinpointEvent().getEvent_type(), "_SMS.FAILURE")) {
            if (event.getPinpointEvent().getAttributes().containsKey("record_status")) {
                String recordStatus = event.getPinpointEvent().getAttributes().get("record_status");
                /*if(EnumSet.of(PinpointRecordStatus.UNREACHABLE,
                        PinpointRecordStatus.UNKNOWN,
                        PinpointRecordStatus.CARRIER_UNREACHABLE,
                        PinpointRecordStatus.CARRIER_BLOCKED,
                        PinpointRecordStatus.NO_QUOTA_LEFT,
                        PinpointRecordStatus.MAX_PRICE_EXCEEDED,
                        PinpointRecordStatus.TTL_EXPIRED).contains(PinpointRecordStatus.valueOf(recordStatus))){*/
                log.error(PINPOINT_SMS_ERROR, "Unable to send the message via Pinpoint for {}", event.getPinpointEvent().getAttributes().get("destination_phone_number"));
                SmsRequest request = redisDataService.get(event.getPinpointEvent().getAttributes().get("message_id"));
                if (Objects.nonNull(request)) {
                    sendThroughFallback(request, PINPOINT_SENDER_BEAN);
                } else {
                    log.info("Message request not found in redis.");
                }
                //}
                log.info("Response from Pinpoint for {}", event.getPinpointEvent().getAttributes().get("destination_phone_number"));
            }
        }
    }

    private void sendThroughFallback(SmsRequest request, String beanName) {
        try {
            Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientDetailsCachingService.getClientByService(request.getService());
            }
            if (Objects.nonNull(client)) {
                final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
                final SenderConfigurations senderConfigurations = senderConfigCachingService.getSenderConfigurationsByAliasAndCountry(client.getAlias(), countryCode);
                if (Objects.nonNull(senderConfigurations)) {
                    Map<CommunicationType, SenderDetails> senderDetailsMap = senderConfigurations.getDetails().get(request.getPriority());
                    if (!CollectionUtils.isEmpty(senderDetailsMap) && senderDetailsMap.containsKey(request.getCommunicationType()) && senderDetailsMap.get(request.getCommunicationType()).isPrimaryPresent()) {
                        final String primarySenderId = senderDetailsMap.get(request.getCommunicationType()).getPrimary();
                        if (senderDetailsMap.get(request.getCommunicationType()).isSecondaryPresent()) {
                            final String secondarySenderId = senderDetailsMap.get(request.getCommunicationType()).getSecondary();
                            if (StringUtils.equalsIgnoreCase(primarySenderId, beanName)) {
                                findSenderBean(request, secondarySenderId);
                            } else if (StringUtils.equalsIgnoreCase(secondarySenderId, beanName)) {
                                log.info("No fallback configured after secondary sender.");
                            } else {
                                findSenderBean(request, primarySenderId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(SEND_THROUGH_FALLBACK_ERROR, e.getMessage(), e);
        }
    }

    private void findSenderBean(SmsRequest request, String primarySenderId) throws Exception {
        try {
            BeanLocatorFactory.getBean(primarySenderId, new ParameterizedTypeReference<IMessageSender<SmsRequest>>() {
            }).sendMessage(request);
        } catch (Exception e) {
            log.error(SMS_SEND_BEAN_ERROR, "error while adding " + primarySenderId + " bean.");
        }
    }

    private Messages getMessage(String messageId, String circleCode) {
        if (StringUtils.equalsIgnoreCase(circleCode, SMSConstants.DEFAULT)) {
            return messageCachingService.get(messageId);
        } else {
            Messages message = messageCachingService.get(messageId.concat(circleCode));
            if (Objects.isNull(message)) {
                return messageCachingService.get(messageId);
            }
            return message;
        }
    }

    @EventListener
    @AnalyseTransaction(name = "whatsappOrderDetailsEvent")
    public void onOrderDetailsRespEvent(WhatsappOrderDetailsEvent event) {
        AnalyticService.update(event);
        try {
            final OrderDetailsRespEvent orderDetailsRespEvent = OrderDetailsRespEvent.builder()
                    .sessionId(event.getMessage().getSessionId())
                    .from(event.getMessage().getFrom())
                    .to(event.getMessage().getTo())
                    .message(OrderDetailsRespEvent.Message.builder()
                            .orderId(event.getResponse().getMessageRequestId())
                            .referenceId(event.getMessage().getOrderDetails().getReferenceId())
                            .build())
                    .type(event.getMessage().getType().toLowerCase())
                    .build();
            final String payload = objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS).writeValueAsString(orderDetailsRespEvent);
            AnalyticService.update(SMSConstants.ORDER_DETAILS_RESP_EVENT, payload);
            final List<Header> headers = new ArrayList() {{
                add(new RecordHeader(BaseConstants.ORG_ID, event.getOrgId().getBytes()));
                add(new RecordHeader(BaseConstants.SERVICE_ID, event.getServiceId().getBytes()));
                add(new RecordHeader(BaseConstants.SESSION_ID, event.getSessionId().getBytes()));
                add(new RecordHeader(BaseConstants.REQUEST_ID, event.getRequestId().getBytes()));
            }};
            kafkaEventPublisher.publish(whatsappInboundTopic, null, System.currentTimeMillis(), UUIDs.timeBased().toString(), payload, headers);
        } catch (Exception e) {
            log.error(SmsLoggingMarkers.KAFKA_PUBLISHER_FAILURE, "Unable to publish the order details response event in kafka due to {}", e.getMessage(), e);
            throw new WynkRuntimeException(SmsErrorType.WHSMS004, e);
        }
    }
}
