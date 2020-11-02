package in.wynk.sms.model;

import in.wynk.sms.constants.SMSPriority;
import in.wynk.sms.constants.SMSSource;
import in.wynk.sms.util.SMSUtils;

@Deprecated
public class MediumPrioritySmsDto extends SMSDto {

    // For automatic creation of beans default constructor is required
    public MediumPrioritySmsDto() {
    }

    public MediumPrioritySmsDto(String messString, String msisdn, boolean useDnd, String source, boolean nineToNine, Integer retryCount) {
        this.setUseDnd(useDnd);
        this.setId(SMSUtils.generateUniqueID(msisdn));
        this.setMessage(messString);
        this.setSource(source);
        this.setShortCode(SMSSource.getShortCodeForLowMediumPriorityFromName(source));
        this.setNineToNine(nineToNine);
        this.setPriority(SMSPriority.MEDIUM.name());

        this.setCreationTimestamp(System.currentTimeMillis());
        //msisdn = SMSUtils.getTenDigitMsisdn(msisdn);
        this.setMsisdn(msisdn);
        this.setRetryCount(retryCount);
    }

}