package in.wynk.sms.utils;

import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.sms.common.constant.SMSPriority;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.entity.SenderDetails;
import in.wynk.sms.core.service.MessageCachingService;
import in.wynk.sms.core.service.SenderConfigurationsCachingService;
import in.wynk.sms.dto.request.CommunicationType;
import in.wynk.sms.enums.MessageTypeSuffix;

import java.util.Map;
import java.util.Objects;

public class SMSUtils {

    public static String getShortCode (String templateId, SMSPriority priority, String clientAlias, String shortCode, String countryCode) {
        final Map<SMSPriority, Map<CommunicationType, SenderDetails>> priorityMap = BeanLocatorFactory.getBean(SenderConfigurationsCachingService.class).getSenderConfigurationsByAliasAndCountry(clientAlias, countryCode).getDetails();
        if(priorityMap.containsKey(priority)){
            final SenderDetails senderDetails = priorityMap.get(priority).get(CommunicationType.SMS);
            if(Objects.nonNull(senderDetails)){
                shortCode = (Objects.nonNull(senderDetails.getShortCode())) ? senderDetails.getShortCode() : shortCode;
                if(Objects.nonNull(templateId)){
                    Messages messages = BeanLocatorFactory.getBean(MessageCachingService.class).get(templateId);
                    messages = (Objects.isNull(messages))? BeanLocatorFactory.getBean(MessageCachingService.class).getMessageByTemplateId(templateId) : messages;
                    shortCode = (Objects.nonNull(messages.getLinkedHeader())) ? messages.getLinkedHeader() : shortCode;
                }
            }
        }
        return shortCode;
    }


    public static String getSuffixedShortCode (String templateId, String shortCode, SMSPriority smsPriority) {
            if(Objects.nonNull(templateId)){
                    Messages messages = BeanLocatorFactory.getBean(MessageCachingService.class).get(templateId);
                    messages = (Objects.isNull(messages))? BeanLocatorFactory.getBean(MessageCachingService.class).getMessageByTemplateId(templateId) : messages;
                    if(Objects.nonNull(messages.getMessageType())){
                        switch (messages.getMessageType()){
                            case TRANSACTIONAL: return shortCode+ MessageTypeSuffix.TRANSACTIONAL.getSuffix();
                            case PROMOTIONAL: return shortCode+ MessageTypeSuffix.PROMOTIONAL.getSuffix();
                            case SERVICE_EXPLICIT:
                            case SERVICE_IMPLICIT:
                                return shortCode+ MessageTypeSuffix.SERVICE.getSuffix();
                            case UNKNOWN: return shortCode;
                            default:return shortCode;
                        }
                    }
                }

        if(Objects.nonNull(smsPriority)) {
            switch (smsPriority) {
                case HIGHEST:
                    return shortCode + MessageTypeSuffix.TRANSACTIONAL.getSuffix();
                case HIGH:
                    return shortCode + MessageTypeSuffix.SERVICE.getSuffix();
                case MEDIUM:
                case LOW:
                    return shortCode + MessageTypeSuffix.PROMOTIONAL.getSuffix();
                default:return shortCode;
            }
        }

     return shortCode;
    }
}