package in.wynk.sms.config.factory;

import in.wynk.smpp.config.properties.SmppProperties;
import in.wynk.smpp.core.connection.factory.ConnectionManagerFactory;
import in.wynk.smpp.core.connection.factory.SmscConnectionFactoryBean;
import in.wynk.smpp.core.generators.SmppResultGenerator;
import in.wynk.smpp.core.receiver.DeliveryReportConsumer;
import in.wynk.smpp.core.sender.ClientFactory;
import in.wynk.smpp.core.sender.TypeOfAddressParser;

import java.util.List;

public class SmscClientConnectionFactoryBean extends SmscConnectionFactoryBean {


    public SmscClientConnectionFactoryBean(SmppProperties smppProperties, SmppResultGenerator smppResultGenerator, List<DeliveryReportConsumer> deliveryReportConsumers, TypeOfAddressParser typeOfAddressParser, ClientFactory clientFactory, ConnectionManagerFactory connectionManagerFactory) {
        super(smppProperties, smppResultGenerator, deliveryReportConsumers, typeOfAddressParser, clientFactory, connectionManagerFactory);
    }
}
