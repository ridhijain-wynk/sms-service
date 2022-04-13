package in.wynk.sms.constants;

import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;

public interface SMSConstants {

    String SMS_QUEUE = "sms_queue";
    String SMS_ENCRYPTION_TOKEN = "SMS_ENCRYPTION_TOKEN";
    String AIRTEL_IQ_SMS_SENDER_BEAN = "airtelIQSmsSender";
    String AIRTEL_SMS_SENDER = "smsSender";
    String MESSAGE_STRATEGY_IQ = "airtelIQ";
    String PLACE_HOLDER_PATTERN = "\\{#var#\\}";
    String REPLACE_PATTERN = "(.+)";
    String MESSAGE_TEXT = "messageText";
    String CONVERTED_MESSAGE_TEXT = "convertedMessageText";
    String TIME_NOT_VALID_FOR_MESSAGE_TYPE = "TIME_NOT_VALID_FOR_MESSAGE_TYPE";
    String FAILURE_CODE = "errorCode";
    String MESSAGE_DATA_MAP = "data";
    String CIRCLE_CODE = "circleCode";
    String DEFAULT = "Default";
    String REMINDER_COUNT = "reminderCount";
    ParserContext SMS_MESSAGE_TEMPLATE_CONTEXT = new TemplateParserContext("${", "}");

}
