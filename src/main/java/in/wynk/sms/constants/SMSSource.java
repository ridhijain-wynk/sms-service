package in.wynk.sms.constants;


import org.apache.commons.lang3.StringUtils;

import static in.wynk.sms.constants.SMSPriority.HIGH;

public enum SMSSource {

	WYNK("WYNK","WYNK","A$-WYNKED", "A$-650018"),
	VIDEO("VIDEO","VIDEO","A$-WYNKED", "A$-650018"),
	GAMES("GAMES","GAMES","A$-WYNKED", "A$-650018"),
	MYAIRTEL("MYAIRTEL","MYAIRTEL", "A$-AIRAPP", "A$-650018"),
	WIFI("WIFI","WIFI","A$-AIRSTR", "A$-650018"),
	AIRTEL_MOVIES("AIRTEL_MOVIES","AIRTEL_MOVIES", "A$-AIRMOV", "A$-650018"),
	AIRTEL_GAMES("AIRTEL_GAMES","AIRTEL_GAMES", "A$-AIRGAM", "A$-650018"),
	AIRTEL_VIDEO("AIRTEL_VIDEOS","AIRTEL_VIDEO","A$-AIRVID", "A$-650018"),
	AIRTEL_TV("AIRTEL_TV","airtelxstream","A$-ARTLTV", "A$-650018"),
	AIRTEL_BOOKS("AIRTEL_BOOKS","AIRTEL_BOOKS","A$-ABOOKS", "A$-650018");


	
	private String name;
	private String alias;
	private String shortCode;
	private String shortCodeForLowMediumPriority;
	
	SMSSource(String name, String alias, String shortCode, String shortCodeForLowMediumPriority) {
		this.name = name;
		this.alias = alias;
		this.shortCode = shortCode;
		this.shortCodeForLowMediumPriority = shortCodeForLowMediumPriority;
	}

	public String getShortCode() {
		return shortCode;
	}

	public String getName() {
		return name;
	}

	public String getShortCodeForLowMediumPriority() {
		return shortCodeForLowMediumPriority;
	}

	public static String getShortCodeFromName(String name) {
		for (SMSSource source:SMSSource.values()) {
			if (source.getName().equalsIgnoreCase(name)) {
				return source.getShortCode();
			}
		}
		return null;
	}

	public static String getShortCode(String name, SMSPriority priority) {
		for (SMSSource source:SMSSource.values()) {
			if (StringUtils.equalsAnyIgnoreCase(name, source.alias, source.name)) {
				if(HIGH.equals(priority)){
					return source.getShortCode();
				} else {
					return source.getShortCodeForLowMediumPriority();
				}
			}
		}
		return null;
	}

	public static String getShortCodeForLowMediumPriorityFromName(String name) {
		for (SMSSource source:SMSSource.values()) {
			if (source.getName().equalsIgnoreCase(name)) {
				return source.getShortCodeForLowMediumPriority();
			}
		}
		return null;
	}
	
	public  static boolean isValidSource(String name) {
		for (SMSSource source:SMSSource.values()) {
			if (source.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
}
