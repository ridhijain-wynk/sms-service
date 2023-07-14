package in.wynk.sms.core.service;

import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.dto.MessageDetails;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.enums.SmsErrorType;
import in.wynk.sms.sender.IMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static in.wynk.sms.constants.SMSConstants.PRIMARY;
import static in.wynk.sms.constants.SMSConstants.SECONDARY;
import static in.wynk.sms.constants.SmsLoggingMarkers.PRIMARY_SENDER_ERROR;
import static in.wynk.sms.constants.SmsLoggingMarkers.SECONDARY_SENDER_ERROR;

@Slf4j
@Component
public class MessageSenderHandler implements ISenderHandler<MessageDetails> {

    @Override
    public void handle (MessageDetails messageDetails) throws Exception {
        Map<String, IMessageSender<SmsRequest>> senderMap = messageDetails.getSenderMap();
        if(Objects.isNull(senderMap) || !senderMap.containsKey(PRIMARY)){
            throw new WynkRuntimeException(SmsErrorType.SMS001);
        }
        switchToSecondaryOnRetry(messageDetails, senderMap);
        try{
            senderMap.get(PRIMARY).sendMessage(messageDetails.getMessage());
        } catch (Exception e) {
            log.error(PRIMARY_SENDER_ERROR, "Error in primary sender {}. Retrying with secondary sender, if present.", e.getMessage(), e);
            if(!senderMap.containsKey(SECONDARY)){
                log.error(SECONDARY_SENDER_ERROR, "Secondary sender not found for client {} ", messageDetails.getMessage().getClientAlias());
                throw e;
            }
            try{
                senderMap.get(SECONDARY).sendMessage(messageDetails.getMessage());
            } catch (Exception ex) {
                log.error(SECONDARY_SENDER_ERROR, "Error in secondary sender {}", ex.getMessage(), e);
                throw ex;
            }
        }
    }

    private static void switchToSecondaryOnRetry (MessageDetails messageDetails, Map<String, IMessageSender<SmsRequest>> senderMap) throws Exception {
        try{
            if(messageDetails.getMessage().getRetryCount() > 0){
                senderMap.get(SECONDARY).sendMessage(messageDetails.getMessage());
            }
        } catch (Exception ex){
            log.error(SECONDARY_SENDER_ERROR, "Error in secondary sender {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}