package in.wynk.sms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PinpointRecordStatus {
    SUCCESSFUL("The message was accepted by the carrier."),
    DELIVERED("The message was delivered to the recipient's device."),
    PENDING("The message hasn't yet been delivered to the recipient's device."),
    INVALID("The destination phone number is invalid."),
    UNREACHABLE("The recipient's device is currently unreachable or unavailable."),
    UNKNOWN("An error occurred that prevented the delivery of the message."),
    BLOCKED("The recipient's device is blocking SMS messages from the origination number."),
    CARRIER_UNREACHABLE("An issue with the mobile network of the recipient prevented the message from being delivered."),
    SPAM("The recipient's mobile carrier identified the contents of the message as spam and blocked delivery of the message."),
    INVALID_MESSAGE("The body of the SMS message is invalid and can't be delivered."),
    CARRIER_BLOCKED("The recipient's carrier has blocked delivery of this message. Malicious content."),
    TTL_EXPIRED("The SMS message couldn't be delivered within the time frame."),
    MAX_PRICE_EXCEEDED("Monthly SMS spending quota exceeded for your account."),
    NO_QUOTA_LEFT("Failed to send because No quota left for account");

    private final String description;
}