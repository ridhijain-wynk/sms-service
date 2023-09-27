package in.wynk.sms.enums;

import in.wynk.exception.IWynkErrorType;
import in.wynk.sms.constants.SmsLoggingMarkers;
import org.slf4j.Marker;
import org.springframework.http.HttpStatus;

public enum SmsErrorType implements IWynkErrorType {
    IQSMS001("SMS Failure", "No Template matched with given message", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.NO_TEMPLATE_FOUND),
    IQSMS002("SMS Failure", "Sms Delivery failed", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.SMS_ERROR),
    IQSMS003("SMS Failure", "Unable to fetch response from airtel IQ", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.REST_TEMPLATE_SMS_ERROR),
    SMS001("SMS Failure", "No Sender found for the client", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.SENDER_NOT_FOUND),
    SMS002("SMS Failure", "Template ID or Header not found for the message", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.NO_TEMPLATE_FOUND),
    SMS003("SMS Failure", "Message type sending not enabled for the client", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.MESSAGE_TYPE_SENDING_NOT_ENABLED),
    PPSMS001("SMS Failure", "Unable to send the message via Pinpoint", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.PINPOINT_SMS_ERROR),
    WHSMS001("Invalid Whatsapp Message Received", "Whatsapp Send Message received is not correct", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.INVALID_WHATSAPP_MESSAGE_RECEIVED),
    WHSMS002("Sending Whatsapp Message Failed", "Unable to send the whatsapp message", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.SEND_WHATSAPP_MESSAGE_FAILED)
    ;

    private final String errorTitle;

    /**
     * The error msg.
     */
    private final String errorMsg;

    /**
     * The http response status.
     */
    private final HttpStatus httpResponseStatusCode;

    private final Marker marker;

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    @Override
    public String getErrorCode() {
        return this.name();
    }

    /**
     * Gets the error title.
     *
     * @return the error title
     */
    @Override
    public String getErrorTitle() {
        return errorTitle;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    @Override
    public String getErrorMessage() {
        return errorMsg;
    }

    /**
     * Gets the http response status.
     *
     * @return the http response status
     */
    @Override
    public HttpStatus getHttpResponseStatusCode() {
        return httpResponseStatusCode;
    }

    @Override
    public Marker getMarker() {
        return marker;
    }

    @Override
    public String toString() {
        return "{" + "errorTitle:'" + errorTitle + '\'' + ", errorMsg:'" + errorMsg + '\'' + ", httpResponseStatusCode" + httpResponseStatusCode + '}';
    }

    SmsErrorType(String errorTitle, String errorMsg, HttpStatus httpResponseStatus, Marker marker) {
        this.errorTitle = errorTitle;
        this.errorMsg = errorMsg;
        this.httpResponseStatusCode = httpResponseStatus;
        this.marker = marker;
    }
}
