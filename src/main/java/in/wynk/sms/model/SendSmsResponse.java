package in.wynk.sms.model;

import in.wynk.sms.model.enums.SMSStatus;

@Deprecated
public class SendSmsResponse {
	
	private SMSDto sms;
	private SMSStatus status;
	private long totalDeliveryTime;
	private long totalQueueTime;
	
	public SendSmsResponse() {
		
	}
	
	public SendSmsResponse(SMSDto sms , SMSStatus status) {
		this.sms = sms;
		this.status = status;
		
		// Initially both Delivery and Status time will be 0
		this.totalDeliveryTime = 0L;
		this.totalQueueTime = 0L;
	}
	
	public SMSDto getSms() {
		return sms;
	}

	public SMSStatus getStatus() {
		return status;
	}

	public void setStatus(SMSStatus status) {
		this.status = status;
	}

	public long getTotalDeliveryTime() {
		return totalDeliveryTime;
	}

	public void setTotalDeliveryTime(long totalDeliveryTime) {
		this.totalDeliveryTime = totalDeliveryTime;
	}

	public long getTotalQueueTime() {
		return totalQueueTime;
	}

	public void setTotalQueueTime(long totalQueueTime) {
		this.totalQueueTime = totalQueueTime;
	}

	@Override
	public String toString() {
		return "SendSmsResponse{" +
				"sms=" + sms +
				", status=" + status +
				", totalDeliveryTime=" + totalDeliveryTime +
				", totalQueueTime=" + totalQueueTime +
				'}';
	}
}
