package in.wynk.sms.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import in.wynk.common.constant.BaseConstants;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.WhatsappRequestWrapper;
import in.wynk.sms.dto.request.WhatsappRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.stream.producer.IKafkaEventPublisher;
import in.wynk.wynkservice.core.dao.entity.WynkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

import static in.wynk.sms.common.constant.SMSPriority.HIGHEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappService {

    @Value("${wynk.kafka.producers.whatsapp.iq.send.message.topic}")
    private String kafkaSendMessageTopic;

    private final SendersCachingService sendersCachingService;
    private final IEntityCacheService<WynkService, String> serviceCache;
    private final IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper> whatsappMessageTransform;
    private final IKafkaEventPublisher<String, AbstractWhatsappOutboundMessage> kafkaEventPublisher;

    public String process(WhatsappRequest request, String clientAlias){
        final Senders senders = sendersCachingService.getSenderByNameClientCountry(SMSConstants.WHATSAPP_SENDER_BEAN, clientAlias, HIGHEST, SMSConstants.DEFAULT_COUNTRY_CODE);
        if(Objects.nonNull(senders)){
            if(Objects.isNull(senders.getWABANumber())){
                throw new WynkRuntimeException(SmsErrorType.WHSMS005);
            }
            final AbstractWhatsappOutboundMessage whatsappOutboundMessage = whatsappMessageTransform.transform(WhatsappRequestWrapper.builder()
                    .clientAlias(clientAlias)
                    .WABANumber(senders.getWABANumber())
                    .request(request).build());
            return publishEventInKafka(kafkaSendMessageTopic, request.getService(), whatsappOutboundMessage);
        }
        throw new WynkRuntimeException(SmsErrorType.WHSMS006);
    }

    private String publishEventInKafka(String topic, String service, AbstractWhatsappOutboundMessage message){
        try{
            final String requestId = UUID.randomUUID().toString();
            final WynkService wynkService = serviceCache.get(service);
            final RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader(BaseConstants.SERVICE_ID, service.getBytes()));
            headers.add(new RecordHeader(BaseConstants.ORG_ID, wynkService.getLinkedClient().getBytes()));
            headers.add(new RecordHeader(BaseConstants.REQUEST_ID, requestId.getBytes()));
            kafkaEventPublisher.publish(topic, null, System.currentTimeMillis(), Uuids.timeBased().toString(),
                    message,
                    headers);
            return requestId;
        } catch(Exception e){
            log.error(SmsLoggingMarkers.KAFKA_PUBLISHER_FAILURE, "Unable to publish the event in kafka due to {}", e.getMessage(), e);
            throw new WynkRuntimeException(SmsErrorType.WHSMS004, e);
        }
    }
}
