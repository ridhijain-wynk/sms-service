package in.wynk.sms.enums;

import in.wynk.exception.IWynkErrorType;
import in.wynk.sms.constants.SmsLoggingMarkers;
import org.slf4j.Marker;
import org.springframework.http.HttpStatus;

public enum SmsErrorType implements IWynkErrorType {
    IQSMS001("SMS Failure", "No Template matched with given message", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.NO_TEMPLATE_FOUND),
    IQSMS002("SMS Failure", "Sms Delivery failed", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.SMS_ERROR),
    IQSMS003("SMS Failure", "Unable to fetch response from airtel IQ", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.REST_TEMPLATE_SMS_ERROR),
    IQSMS004("SMS Failure", "No Sender found for the client", HttpStatus.INTERNAL_SERVER_ERROR, SmsLoggingMarkers.PRIMARY_SENDER_ERROR)
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
