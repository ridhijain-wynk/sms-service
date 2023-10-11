package in.wynk.sms.service;

import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.dto.wa.outbound.AbstractWhatsappOutboundMessage;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.WhatsappRequestWrapper;
import in.wynk.sms.dto.request.WhatsappRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.stream.producer.IKafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static in.wynk.sms.common.constant.SMSPriority.HIGHEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappMessageService {

    @Value("${wynk.kafka.producers.whatsapp.iq.send.message.topic}")
    private String kafkaSendMessageTopic;

    private final SendersCachingService sendersCachingService;
    private final IWhatsappMessageTransform<AbstractWhatsappOutboundMessage, WhatsappRequestWrapper> whatsappMessageTransform;
    private final IKafkaEventPublisher<String, AbstractWhatsappOutboundMessage> kafkaEventPublisher;
    public void process(WhatsappRequest request, String clientAlias){
        final Senders senders = sendersCachingService.getSenderByNameClientCountry(SMSConstants.WHATSAPP_SENDER_BEAN, clientAlias, HIGHEST, SMSConstants.DEFAULT_COUNTRY_CODE);
        if(Objects.nonNull(senders)){
            if(Objects.nonNull(senders.getWABANumber())){
                AbstractWhatsappOutboundMessage whatsappOutboundMessage = whatsappMessageTransform.transform(WhatsappRequestWrapper.builder()
                        .clientAlias(clientAlias)
                        .WABANumber(senders.getWABANumber())
                        .request(request).build());
                publishEventInKafka(kafkaSendMessageTopic, request.getService(), whatsappOutboundMessage);
            }
            throw new WynkRuntimeException(SmsErrorType.WHSMS005);
        }
        throw new WynkRuntimeException(SmsErrorType.WHSMS006);
    }

    private void publishEventInKafka(String topic, String service, AbstractWhatsappOutboundMessage message){
        try{
            final RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader(BaseConstants.SERVICE_ID, service.getBytes()));
            kafkaEventPublisher.publish(topic, null, null, null,
                    message,
                    headers);
        } catch(Exception ignored){}
    }
}
