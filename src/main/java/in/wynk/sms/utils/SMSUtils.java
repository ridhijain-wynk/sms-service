package in.wynk.sms.utils;

import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.dto.request.CommunicationType;
import java.util.Map;
import java.util.Objects;

public class SMSUtils {

    public static String getShortCode (String templateId, SMSPriority priority, String clientAlias, String shortCode) {
        Map<SMSPriority, Map<CommunicationType, SenderDetails>> priorityMap = BeanLocatorFactory.getBean(SenderConfigurationsCachingService.class).getSenderConfigurationsByAlias(clientAlias).getDetails();
        if(priorityMap.containsKey(priority)){
            SenderDetails senderDetails = priorityMap.get(priority).get(CommunicationType.SMS);
            if(Objects.nonNull(senderDetails)){
                shortCode = (Objects.nonNull(senderDetails.getShortCode())) ? senderDetails.getShortCode() : shortCode;
                if(Objects.nonNull(templateId)){
                    Messages messages = BeanLocatorFactory.getBean(MessageCachingService.class).get(templateId);
                    shortCode = (Objects.nonNull(messages.getLinkedHeader())) ? messages.getLinkedHeader() : shortCode;
                }
            }
        }
        return shortCode;
    }
}