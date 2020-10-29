package in.wynk.sms.model.enums;

import org.apache.commons.lang3.StringUtils;

public enum SMSPriority {

	// REDIS PQ STORES in ascending order of score. Highest priority SMS should be at starting of Queue.
	HIGH("-1", 0),
	MEDIUM("10", 60),
	LOW("100", 120);
	private String score;
	private int delay;
	
	SMSPriority(String score, int delay) {
		this.setScore(score);
		this.setDelay(delay);
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public static int delay(String priority){
		for (SMSPriority p: values()){
			if(StringUtils.equalsIgnoreCase(priority, p.name())){
				return p.getDelay();
			}
		}
		return MEDIUM.getDelay();
	}

}
