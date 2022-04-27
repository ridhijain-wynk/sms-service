package in.wynk.sms.receiver;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.smpp.core.dto.DeliveryReport;
import in.wynk.smpp.core.receiver.DeliveryReportConsumer;

public class DefaultDeliveryReportConsumer implements DeliveryReportConsumer {

    @Override
    @AnalyseTransaction(name = "smppDeliveryReport")
    public void accept(DeliveryReport deliveryReport) {
        AnalyticService.update(deliveryReport);
    }
}
