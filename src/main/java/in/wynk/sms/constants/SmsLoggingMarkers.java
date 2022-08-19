package in.wynk.sms.constants;

import in.wynk.logging.BaseLoggingMarkers;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface SmsLoggingMarkers extends BaseLoggingMarkers {

    Marker MULTI_SMPP_BEAN_DEFINITION_REGISTRATION = MarkerFactory.getMarker("MULTI_SMPP_BEAN_DEFINITION_REGISTRATION");
    Marker MULTI_SMPP_BEAN_REFERENCE_RESOLUTION = MarkerFactory.getMarker("MULTI_SMPP_BEAN_REFERENCE_RESOLUTION");
    Marker PROMOTIONAL_MSG_ERROR = MarkerFactory.getMarker("PROMOTIONAL_MSG_ERROR");
    Marker HIGHEST_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("HIGHEST_PRIORITY_SMS_ERROR");
    Marker HIGH_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("HIGH_PRIORITY_SMS_ERROR");
    Marker MEDIUM_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("MEDIUM_PRIORITY_SMS_ERROR");
    Marker LOW_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("LOW_PRIORITY_SMS_ERROR");
    Marker PRIMARY_SENDER_ERROR = MarkerFactory.getMarker("PRIMARY_SENDER_ERROR");
    Marker SECONDARY_SENDER_ERROR = MarkerFactory.getMarker("SECONDARY_SENDER_ERROR");
    Marker SL_SMS_ERROR = MarkerFactory.getMarker("SL_SMS_ERROR");
    Marker SMS_SEND_BEAN_ERROR = MarkerFactory.getMarker("SMS_SEND_BEAN_ERROR");
    Marker NO_TEMPLATE_FOUND= MarkerFactory.getMarker("NO_TEMPLATE_FOUND");
    Marker SMS_ERROR= MarkerFactory.getMarker("SMS_ERROR");
    Marker REST_TEMPLATE_SMS_ERROR= MarkerFactory.getMarker("REST_TEMPLATE_SMS_ERROR");
    Marker MESSAGE_NOT_FOUND = MarkerFactory.getMarker("MESSAGE_NOT_FOUND");
    Marker OLD_MESSAGE_PATTERN = MarkerFactory.getMarker("OLD_MESSAGE_PATTERN");
}
