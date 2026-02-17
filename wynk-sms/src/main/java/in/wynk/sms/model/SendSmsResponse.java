package in.wynk.sms.model;

import lombok.Getter;

@Deprecated
@Getter
public class SendSmsResponse {

    private final String status = "QUEUED";
    private final static SendSmsResponse RESPONSE = new SendSmsResponse();

    public static SendSmsResponse defaultResponse() {
        return RESPONSE;
    }
}
