package in.wynk.sms.constants;

import in.wynk.logging.BaseLoggingMarkers;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface SmsMarkers extends BaseLoggingMarkers {

    Marker SQS_CONSUMER_ERROR = MarkerFactory.getMarker("SQS_CONSUMER_ERROR");
    Marker SQS_PRODUCER_ERROR = MarkerFactory.getMarker("SQS_PRODUCER_ERROR");
    Marker SQS_CONFIG_ERROR = MarkerFactory.getMarker("SQS_CONFIG_ERROR");
    Marker PROMOTIONAL_MSG_ERROR = MarkerFactory.getMarker("PROMOTIONAL_MSG_ERROR");
    Marker CONNECTION_POOL_TIMEOUT_ERROR = MarkerFactory.getMarker("CONNECTION_POOL_TIMEOUT_ERROR");
    Marker TRANSACTIONAL_MSG_ERROR = MarkerFactory.getMarker("TRANSACTIONAL_MSG_ERROR");
}
