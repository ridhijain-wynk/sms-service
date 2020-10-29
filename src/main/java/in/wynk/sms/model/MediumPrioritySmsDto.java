package in.wynk.sms.model;

import in.wynk.sms.model.enums.SMSPriority;
import in.wynk.sms.model.enums.SMSSource;
import in.wynk.sms.util.SMSUtils;

public class MediumPrioritySmsDto extends SMSDto {
	
	// For automatic creation of beans default constructor is required
	public MediumPrioritySmsDto() {
	}
	
	public MediumPrioritySmsDto(String messString, String msisdn, boolean useDnd, String source, boolean nineToNine,Integer retryCount) {
		this.setUseDnd(useDnd);
		this.setId(SMSUtils.generateUniqueID(msisdn));
		this.setMessage(messString);
		this.setSource(source);
		this.setShortCode(SMSSource.getShortCodeForLowMediumPriorityFromName(source));
		this.setNineToNine(nineToNine);
		
		// Redis Sorted set by default does lexographic sorting for elements of same score. 
		// To main the order of incoming request adding time in score.
		this.setPriorityScore(( SMSUtils.generateScoreWithTime(SMSPriority.MEDIUM.getScore())));
		this.setPriority(SMSPriority.MEDIUM.name());
		
		this.setCreationTimestamp(System.currentTimeMillis());
		//msisdn = SMSUtils.getTenDigitMsisdn(msisdn);
		this.setMsisdn(msisdn);
		this.setRetryCount(retryCount);
	}

}