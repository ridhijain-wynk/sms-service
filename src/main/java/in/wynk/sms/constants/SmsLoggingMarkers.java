package in.wynk.sms.constants;

import in.wynk.logging.BaseLoggingMarkers;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface SmsLoggingMarkers extends BaseLoggingMarkers {


    Marker PROMOTIONAL_MSG_ERROR = MarkerFactory.getMarker("PROMOTIONAL_MSG_ERROR");
    Marker HIGH_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("HIGH_PRIORITY_SMS_ERROR");
    Marker MEDIUM_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("MEDIUM_PRIORITY_SMS_ERROR");
    Marker LOW_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("LOW_PRIORITY_SMS_ERROR");
    Marker SL_SMS_ERROR = MarkerFactory.getMarker("SL_SMS_ERROR");
}
