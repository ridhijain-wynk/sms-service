package in.wynk.sms.sender;

import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.auth.dao.entity.Client;
import in.wynk.client.service.ClientDetailsCachingService;
import in.wynk.common.utils.BeanLocatorFactory;
import in.wynk.exception.WynkRuntimeException;
import in.wynk.sms.constants.SMSConstants;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.service.IScrubEngine;
import in.wynk.sms.dto.request.SmsRequest;
import in.wynk.sms.enums.SmsErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class AbstractSMSSender implements IMessageSender<SmsRequest> {

    protected Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

    public void sendMessage(SmsRequest request) throws Exception {
        try {
            final ClientDetailsCachingService clientCache = BeanLocatorFactory.getBean(ClientDetailsCachingService.class);
            Client client = clientCache.getClientByAlias(request.getClientAlias());
            if (Objects.isNull(client)) {
                client = clientCache.getClientByService(request.getService());
            }
            if (client.<Boolean>getMeta(SMSConstants.MESSAGE_SCRUBBING_ENABLED).orElse(false) || client.<Boolean>getMeta(request.getPriority().getSmsPriority() + SMSConstants.PRIORITY_BASED_MESSAGE_SCRUBBING_ENABLED).orElse(false))
                validate(request);
        } catch (WynkRuntimeException e) {
            if (e.getErrorType() != SmsErrorType.IQSMS001)
                throw e;
            AnalyticService.update("scrubbed", true);
            logger.warn(SmsLoggingMarkers.NO_TEMPLATE_FOUND, "message is scrubbed as no matching template is found {}", request);
            return;
        }
        send(request);
    }

    protected abstract void send(SmsRequest request) throws Exception;

    protected void validate(SmsRequest request) {
        BeanLocatorFactory.getBean(IScrubEngine.class).scrub(request.getText());
    }

}
