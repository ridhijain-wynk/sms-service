package in.wynk.sms.constants;

import in.wynk.common.constant.BaseConstants;

public interface SMSConstants extends BaseConstants {

    String SMS_QUEUE = "sms_queue";

    String SCRUBBING_TEMPLATE_ID = "scrubbingTemplateId";
    String SCRUBBING_ENABLED = "isMessageScrubbingEnabled";

    String IS_MESSAGE_SCRUBBED = "isMessageScrubbed";
    String MESSAGE_SCRUBBING_ENABLED = "MESSAGE_SCRUBBING_ENABLED";
    String SMS_ENCRYPTION_TOKEN = "SMS_ENCRYPTION_TOKEN";
    String AIRTEL_IQ_SMS_SENDER_BEAN = "airtelIQSmsSender";
    String PINPOINT_SENDER_BEAN = "pinpoint";
    String WHATSAPP_SENDER_BEAN = "whatsappSender";
    String AIRTEL_SMS_SENDER = "smsSender";
    String MESSAGE_STRATEGY_IQ = "airtelIQ";
    String LOBBY_MESSAGE_STRATEGY = "lobby";
    String PLACE_HOLDER_PATTERN = "\\{#var#\\}";
    String REPLACE_PATTERN = "{.+}";
    String SPRING_EXP_REPLACE_PATTERN = "(.*?)";
    String MESSAGE_TEXT = "messageText";
    String CONVERTED_MESSAGE_TEXT = "convertedMessageText";
    String TIME_NOT_VALID_FOR_MESSAGE_TYPE = "TIME_NOT_VALID_FOR_MESSAGE_TYPE";
    String FAILURE_CODE = "errorCode";
    String PRIMARY = "PRIMARY";
    String SECONDARY = "SECONDARY";
    String KAFKA_RETRY_COUNT = "retry-count";
    String ORDER_DETAILS_RESP_EVENT = "ORDER_DETAILS_RESP_EVENT";

}
