package in.wynk.sms.core.service;

import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.dto.response.VoiceSmsResponse;

public interface IVoiceSmsService {
    VoiceSmsResponse sendVoiceSms(SmsRequest msisdn);
}
