package in.wynk.sms.sender;

import com.amazonaws.services.pinpoint.AmazonPinpoint;
import com.amazonaws.services.pinpoint.model.*;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.common.constant.Country;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.core.entity.MessageTypeSpecificDetails;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.service.IMessageService;
import in.wynk.sms.core.service.IRedisCacheService;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.core.service.SendersCachingService;
import in.wynk.sms.dto.MessageTemplateDTO;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.enums.MessageType;
import in.wynk.sms.enums.SmsErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.PINPOINT_SENDER_BEAN;
import static in.wynk.sms.constants.SmsLoggingMarkers.*;
import static in.wynk.sms.enums.SmsErrorType.IQSMS001;
import static in.wynk.sms.enums.SmsErrorType.SMS003;

@Slf4j
@RequiredArgsConstructor
@Service(PINPOINT_SENDER_BEAN)
public class PinpointSender extends AbstractSMSSender {

    private final ClientDetailsCachingService clientDetailsCachingService;
    private final SendersCachingService sendersCachingService;
    private final AmazonPinpoint pinpoint;
    private final IMessageService messageService;
    private final IRedisCacheService redisDataService;

    @Override
    @AnalyseTransaction(name = "sendSmsPinpoint")
    public void sendMessage(SmsRequest request) throws Exception {
        super.sendMessage(request);
    }

    @Override
    public void send(SmsRequest request) throws Exception {
        AnalyticService.update(request);
        Client client = clientDetailsCachingService.getClientByAlias(request.getClientAlias());
        if (Objects.isNull(client)) {
            client = clientDetailsCachingService.getClientByService(request.getService());
        }
        if(Objects.nonNull(client)){
            final String countryCode = StringUtils.isNotEmpty(request.getCountryCode()) ? Country.getCountryIdByCountryCode(request.getCountryCode()) : BaseConstants.DEFAULT_COUNTRY_CODE;
            Senders senders = sendersCachingService.getSenderByNameClientCountry(PINPOINT_SENDER_BEAN, client.getAlias(), request.getPriority(), countryCode);
            if(Objects.isNull(senders)){
                log.error(SENDER_NOT_FOUND, "Pinpoint sender not configured for client: {}, country : {}", client.getAlias(), countryCode);
                throw new WynkRuntimeException(SmsErrorType.SMS001);
            }
            if(StringUtils.equalsIgnoreCase(countryCode, BaseConstants.DEFAULT_COUNTRY_CODE)){
                sendMessageDomestic(request, senders);
            } else {
                sendMessageInternational(request, senders);
            }
        }
    }

    private void sendMessageInternational(SmsRequest request, Senders senders) {
        MessageType messageType = getMessageType(request.getPriority());
        if(!CollectionUtils.isEmpty(senders.getMessageTypeDetails()) && senders.getMessageTypeDetails().containsKey(messageType)){
            final MessageTypeSpecificDetails messageTypeSpecificDetails = senders.getMessageTypeDetails().get(messageType);
            if(!messageTypeSpecificDetails.isEnabled()){
                log.info(MESSAGE_TYPE_SENDING_NOT_ENABLED, messageType+ " Message sending via Pinpoint disabled.");
                throw new WynkRuntimeException(SMS003);
            }
            final SMSMessage smsMessage = new SMSMessage()
                    .withBody(request.getText()).withMessageType(messageType.getType())
                    .withOriginationNumber(messageTypeSpecificDetails.getPinpointOriginationNumber())
                    .withSenderId(messageTypeSpecificDetails.getPinpointSenderID())
                    .withKeyword(messageTypeSpecificDetails.getPinpointKeyword());
            String destinationNumber = (request.getMsisdn().contains(request.getCountryCode()))? request.getMsisdn() : request.getCountryCode().concat(request.getMsisdn());
            final MessageResult messageResult = invokeSendMessagesAPI(messageTypeSpecificDetails.getPinpointAppId(), smsMessage, destinationNumber);
            AnalyticService.update(messageResult);
            redisDataService.save(messageResult.getMessageId(), request);
        }
    }

    private void sendMessageDomestic(SmsRequest request, Senders senders) {
        MessageTemplateDTO messageTemplateDTO = messageService.findMessagesFromSmsText(request.getText());
        if (Objects.isNull(messageTemplateDTO)) {
            log.error(NO_TEMPLATE_FOUND, "No template found for message: {}", request.getText());
            throw new WynkRuntimeException(IQSMS001);
        }
        if(!CollectionUtils.isEmpty(senders.getMessageTypeDetails()) && senders.getMessageTypeDetails().containsKey(messageTemplateDTO.getMessageType())){
            final MessageTypeSpecificDetails messageTypeSpecificDetails = senders.getMessageTypeDetails().get(messageTemplateDTO.getMessageType());
            if(!messageTypeSpecificDetails.isEnabled()){
                log.info(MESSAGE_TYPE_SENDING_NOT_ENABLED, messageTemplateDTO.getMessageType().getType()+ " Message sending via Pinpoint disabled.");
                throw new WynkRuntimeException(SMS003);
            }
            final SMSMessage smsMessage = new SMSMessage()
                    .withBody(request.getText()).withMessageType(messageTemplateDTO.getMessageType().getType())
                    .withEntityId(senders.getEntityId()).withTemplateId(messageTemplateDTO.getMessageTemplateId())
                    .withOriginationNumber(messageTypeSpecificDetails.getPinpointOriginationNumber())
                    .withSenderId(messageTypeSpecificDetails.getPinpointSenderID())
                    .withKeyword(messageTypeSpecificDetails.getPinpointKeyword());
            String destinationNumber = (request.getMsisdn().contains(request.getCountryCode()))? request.getMsisdn() : request.getCountryCode().concat(request.getMsisdn());
            final MessageResult messageResult = invokeSendMessagesAPI(messageTypeSpecificDetails.getPinpointAppId(), smsMessage, destinationNumber);
            AnalyticService.update(messageResult);
            redisDataService.save(messageResult.getMessageId(), request);
        }
    }

    public MessageResult invokeSendMessagesAPI(String applicationId, SMSMessage smsMessage, String destinationNumber) {
        final Map<String, AddressConfiguration> addressMap = new HashMap<>();
        addressMap.put(destinationNumber, new AddressConfiguration().withChannelType(ChannelType.SMS));

        final DirectMessageConfiguration directMessageConfiguration = new DirectMessageConfiguration()
                .withSMSMessage(smsMessage);
        final MessageRequest messageRequest = new MessageRequest()
                .withAddresses(addressMap)
                .withMessageConfiguration(directMessageConfiguration);
        final SendMessagesRequest sendMessagesRequest = new SendMessagesRequest()
                .withMessageRequest(messageRequest)
                .withApplicationId(applicationId);
        final SendMessagesResult sendMessagesResponse = pinpoint.sendMessages(sendMessagesRequest);
        final MessageResponse messageResponse = sendMessagesResponse.getMessageResponse();
        final Map<String, MessageResult> messageResult = messageResponse.getResult();
        return messageResult.get(destinationNumber);
    }

    private MessageType getMessageType(SMSPriority priority){
        switch (priority) {
            case HIGHEST:
            case HIGH:
                return MessageType.TRANSACTIONAL;
            case MEDIUM:
            case LOW:
                return MessageType.PROMOTIONAL;
            default:
                return MessageType.UNKNOWN;
        }
    }
}