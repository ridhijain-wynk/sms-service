package in.wynk.sms.constants;

import in.wynk.logging.BaseLoggingMarkers;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface SmsLoggingMarkers extends BaseLoggingMarkers {

    Marker MULTI_SMPP_BEAN_DEFINITION_REGISTRATION = MarkerFactory.getMarker("MULTI_SMPP_BEAN_DEFINITION_REGISTRATION");
    Marker MULTI_SMPP_BEAN_REFERENCE_RESOLUTION = MarkerFactory.getMarker("MULTI_SMPP_BEAN_REFERENCE_RESOLUTION");
    Marker TIME_TAKEN_TO_FIND_SMS = MarkerFactory.getMarker("TIME_TAKEN_TO_FIND_SMS");
    Marker PROMOTIONAL_MSG_ERROR = MarkerFactory.getMarker("PROMOTIONAL_MSG_ERROR");
    Marker HIGHEST_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("HIGHEST_PRIORITY_SMS_ERROR");
    Marker HIGH_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("HIGH_PRIORITY_SMS_ERROR");
    Marker MEDIUM_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("MEDIUM_PRIORITY_SMS_ERROR");
    Marker LOW_PRIORITY_SMS_ERROR = MarkerFactory.getMarker("LOW_PRIORITY_SMS_ERROR");
    Marker PRIMARY_SENDER_ERROR = MarkerFactory.getMarker("PRIMARY_SENDER_ERROR");
    Marker SECONDARY_SENDER_ERROR = MarkerFactory.getMarker("SECONDARY_SENDER_ERROR");
    Marker LUCENE_FIND_SMS_ERROR = MarkerFactory.getMarker("LUCENE_FIND_SMS_ERROR");
    Marker SL_SMS_ERROR = MarkerFactory.getMarker("SL_SMS_ERROR");
    Marker SMS_SEND_BEAN_ERROR = MarkerFactory.getMarker("SMS_SEND_BEAN_ERROR");
    Marker NO_TEMPLATE_FOUND= MarkerFactory.getMarker("NO_TEMPLATE_FOUND");
    Marker SENDER_NOT_FOUND= MarkerFactory.getMarker("SENDER_NOT_FOUND");
    Marker MESSAGE_TYPE_SENDING_NOT_ENABLED= MarkerFactory.getMarker("MESSAGE_TYPE_SENDING_NOT_ENABLED");
    Marker SMS_ERROR= MarkerFactory.getMarker("SMS_ERROR");
    Marker REST_TEMPLATE_SMS_ERROR= MarkerFactory.getMarker("REST_TEMPLATE_SMS_ERROR");
    Marker MESSAGE_NOT_FOUND = MarkerFactory.getMarker("MESSAGE_NOT_FOUND");
    Marker OLD_MESSAGE_PATTERN = MarkerFactory.getMarker("OLD_MESSAGE_PATTERN");
    Marker SENDER_CONFIGURATIONS_CACHING_FAILURE = MarkerFactory.getMarker("SENDER_CONFIGURATIONS_CACHING_FAILURE");
    Marker SENDERS_CACHING_FAILURE = MarkerFactory.getMarker("SENDERS_CACHING_FAILURE");
    Marker PINPOINT_SMS_ERROR = MarkerFactory.getMarker("PINPOINT_SMS_ERROR");
    Marker SEND_THROUGH_FALLBACK_ERROR = MarkerFactory.getMarker("SEND_THROUGH_FALLBACK_ERROR");
    Marker INVALID_WHATSAPP_MESSAGE_RECEIVED = MarkerFactory.getMarker("INVALID_WHATSAPP_MESSAGE_RECEIVED");
    Marker SEND_WHATSAPP_MESSAGE_FAILED = MarkerFactory.getMarker("SEND_WHATSAPP_MESSAGE_FAILED");
}