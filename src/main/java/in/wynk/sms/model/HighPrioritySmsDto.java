package in.wynk.sms.model;

import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.constants.SMSSource;
import in.wynk.sms.util.SMSUtils;

@Deprecated
public class HighPrioritySmsDto extends SMSDto {

    // For automatic creation of beans default constructor is required
    public HighPrioritySmsDto() {
    }

    public HighPrioritySmsDto(String messString, String msisdn, boolean useDnd, String source, boolean nineToNine, Integer retryCount) {
        this.setUseDnd(useDnd);
        this.setId(SMSUtils.generateUniqueID(msisdn));
        this.setMessage(messString);
        this.setSource(source);
        this.setNineToNine(nineToNine);
        this.setShortCode(SMSSource.getShortCodeFromName(source));
        this.setPriority(SMSPriority.HIGH.name());
        //msisdn = SMSUtils.getTenDigitMsisdn(msisdn);
        this.setMsisdn(msisdn);
        this.setCreationTimestamp(System.currentTimeMillis());
        this.setRetryCount(retryCount);
    }

}
