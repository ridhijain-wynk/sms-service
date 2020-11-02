package in.wynk.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.wynk.sms.model.enums.SMSPriority;
import in.wynk.sms.model.enums.SMSSource;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@JsonIgnoreProperties(value = "true")
public class SendSmsRequest implements Serializable {

    private static final long serialVersionUID = -289736810127565940L;

    private String priority;
    private String message;
    private String msisdn;
    private String source;
    private boolean nineToNine = false;
    private boolean useDnd = false;
    private Integer retryCount;
    private String countryCode;
    //Added service field to find out messages being sent by CAPI.
    private String service;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        message = message.replaceAll("[^\\x00-\\x7F]", "");
        message = StringEscapeUtils.unescapeJava(message);
        this.message = message;
    }

    public String getService() {
        return service;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean getUseDnd() {
        return useDnd;
    }

    public void setUseDnd(boolean useDnd) {
        this.useDnd = useDnd;
    }

    public boolean isNineToNine() {
        return nineToNine;
    }

    public void setNineToNine(boolean nineToNine) {
        this.nineToNine = nineToNine;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @JsonIgnore
    public boolean validRequest() {
        if (StringUtils.isAnyBlank(message, priority, msisdn, source)) {
            return false;
        }
        if (!SMSSource.isValidSource(source)) {
            return false;
        }
        return EnumUtils.isValidEnum(SMSPriority.class, priority);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("priority", priority)
                .append("message", message)
                .append("msisdn", msisdn)
                .append("source", source)
                .append("nineToNine", nineToNine)
                .append("useDnd", useDnd)
                .append("retryCount", retryCount)
                .append("countryCode", countryCode)
                .append("service", service)
                .toString();
    }
}
