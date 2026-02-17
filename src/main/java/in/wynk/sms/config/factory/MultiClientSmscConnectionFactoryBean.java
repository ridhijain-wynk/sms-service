package in.wynk.sms.config.factory;

import in.wynk.smpp.config.properties.SmppProperties;
import in.wynk.smpp.core.connection.SmscConnectionsHolder;
import in.wynk.smpp.core.connection.factory.ConnectionManagerFactory;
import in.wynk.smpp.core.connection.factory.SmscConnectionFactoryBean;
import in.wynk.smpp.core.generators.SmppResultGenerator;
import in.wynk.smpp.core.receiver.DeliveryReportConsumer;
import in.wynk.smpp.core.sender.ClientFactory;
import in.wynk.smpp.core.sender.TypeOfAddressParser;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
public class MultiClientSmscConnectionFactoryBean {

    private SmppResultGenerator smppResultGenerator;

    private final ClientFactory clientFactory;
    private final TypeOfAddressParser typeOfAddressParser;
    private final ConnectionManagerFactory connectionManagerFactory;

    private final List<DeliveryReportConsumer> deliveryReportConsumers;

    public SmscConnectionsHolder getObject(SmppProperties smppProperties) {
        return new SmscConnectionFactoryBean(smppProperties, smppResultGenerator, deliveryReportConsumers, typeOfAddressParser, clientFactory, connectionManagerFactory).getObject();
    }

}
