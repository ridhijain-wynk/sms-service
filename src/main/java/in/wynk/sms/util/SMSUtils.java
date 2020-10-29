package in.wynk.sms.util;


import in.wynk.sms.model.HighPrioritySmsDto;
import in.wynk.sms.model.LowPrioritySmsDto;
import in.wynk.sms.model.MediumPrioritySmsDto;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.enums.SMSPriority;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
@Slf4j
public class SMSUtils {


    public static String generateUniqueID(String msisdn) {
        long time = System.currentTimeMillis();
        return msisdn + time;
    }

    public static String getTenDigitMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn))
            return msisdn;
        msisdn = msisdn.trim();
        int length = msisdn.length();
        if (length == 10) {
            return msisdn;
        }
        if (length > 10) {
            return msisdn.substring(length - 10);
        }
        throw new IllegalArgumentException("Illegal value for msisdn : " + msisdn);
    }

    public static Double generateScoreWithTime(String score) {
        Date d = new Date();
        String newscore = score + "." + d.getTime();
        return Double.parseDouble(newscore);
    }

    public static SMSDto getSMSObjectFromJSONString(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject smsObj = (JSONObject) parser.parse(jsonString);
            String msisdn = String.valueOf(smsObj.get("msisdn"));
            String message = String.valueOf(smsObj.get("message"));
            String source = String.valueOf(smsObj.get("source"));
            Boolean useDnd = (boolean) smsObj.get("useDnd");
            Boolean nineToNine = (boolean) smsObj.get("nineToNine");
            String shortCode = String.valueOf(smsObj.get("shortCode"));
            long createTimestmap = (long) smsObj.get("creationTimestamp");
            String id = String.valueOf(smsObj.get("id"));
            double priorityScore = (double) smsObj.get("priorityScore");

            Integer retryCount = 0;
            if (smsObj.get("retryCount") != null) {
                retryCount = Integer.parseInt(smsObj.get("retryCount").toString());
            }

            String priority = String.valueOf(smsObj.get("priority"));
            SMSDto sms = null;
            if (priority.equalsIgnoreCase(SMSPriority.HIGH.name())) {
                sms = new HighPrioritySmsDto();
            } else if (priority.equalsIgnoreCase(SMSPriority.MEDIUM.name())) {
                sms = new MediumPrioritySmsDto();
            } else {
                sms = new LowPrioritySmsDto();
            }
            if (smsObj.get("countryCode") != null) {
                sms.setCountryCode((String) smsObj.get("countryCode"));
            }
            sms.setId(id);
            sms.setMessage(message);
            sms.setMsisdn(msisdn);
            sms.setPriority(priority);
            sms.setPriorityScore(priorityScore);
            sms.setSource(source);
            sms.setShortCode(shortCode);
            sms.setCreationTimestamp(createTimestmap);
            sms.setUseDnd(useDnd);
            sms.setNineToNine(nineToNine);
            sms.setRetryCount(retryCount);
            log.info("Creation Timestamp for string:" + jsonString + ":*****:" + sms.getCreationTimestamp());

            return sms;
        } catch (Exception e) {
            log.error("Error while executing getSMSObjectFromJSONString", e);
            return null;
        }
    }


    public static boolean isvalidSMSDeliveryHour() {
        Date today = new Date();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(today);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 22;
    }

}
