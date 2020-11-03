package in.wynk.sms.model;

@Deprecated
public class SendSmsResponse {

    private final String status = "QUEUED";
    private final static SendSmsResponse RESPONSE = new SendSmsResponse();

    public static SendSmsResponse defaultResponse() {
        return RESPONSE;
    }
}
