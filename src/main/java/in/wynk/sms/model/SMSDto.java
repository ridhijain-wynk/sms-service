package in.wynk.sms.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.json.simple.JSONObject;


// Json Conversion of abstract objects requires this declaration
@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(value = HighPrioritySmsDto.class),
	@Type(value = MediumPrioritySmsDto.class),
	@Type(value = LowPrioritySmsDto.class),
})
public abstract class SMSDto {

	private String id;
	private String message;
	private String msisdn;
	private String source;
	private String shortCode;
	private boolean nineToNine;
	private String priority;
	private Integer retryCount;
	private String countryCode;

	private double priorityScore;
	
	private boolean useDnd;

	private String service;

	// To compute the stats associated SMS
	private long creationTimestamp;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String generateUniqueSMSId() {
		return "";
	}
	
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public boolean isUseDnd() {
		return useDnd;
	}
	public void setUseDnd(boolean useDnd) {
		this.useDnd = useDnd;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public boolean isNineToNine() {
		return nineToNine;
	}
	public void setNineToNine(boolean nineToNine) {
		this.nineToNine = nineToNine;
	}
	public double getPriorityScore() {
		return priorityScore;
	}
	public void setPriorityScore(double priorityScore) {
		this.priorityScore = priorityScore;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public JSONObject toJsonObject() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("message",message);
		jsonObj.put("id", id);
		jsonObj.put("priority", priority);
		jsonObj.put("priorityScore", priorityScore);
		jsonObj.put("msisdn", msisdn);
		jsonObj.put("creationTimestamp", creationTimestamp);
		jsonObj.put("useDnd", useDnd);
		jsonObj.put("source", source);
		jsonObj.put("nineToNine", nineToNine);
		jsonObj.put("shortCode", shortCode);
		jsonObj.put("retryCount",retryCount);
		jsonObj.put("countryCode",countryCode);
		return jsonObj;
	}

	public String toJson() {
		JSONObject jsonObj = toJsonObject();
		return jsonObj.toString();
	}
	
	@Override
	public String toString() {
		return toJson() ;
	}

}
