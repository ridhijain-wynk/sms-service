package in.wynk.sms.core.service;

import in.wynk.sms.dto.request.SmsRequest;

public interface IRedisCacheService {

    SmsRequest save (String messageId, SmsRequest request);
    SmsRequest get (String messageId);
}