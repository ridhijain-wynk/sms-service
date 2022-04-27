package in.wynk.sms.config;

import in.wynk.smpp.config.properties.SmppProperties;
import in.wynk.smpp.core.connection.factory.ConnectionManagerFactory;
import in.wynk.smpp.core.generators.AlwaysSuccessSmppResultGenerator;
import in.wynk.smpp.core.sender.ClientFactory;
import in.wynk.smpp.core.sender.DefaultTypeOfAddressParser;
import in.wynk.smpp.core.sender.manager.StrategySenderManager;
import in.wynk.smpp.core.stretegy.RoutingStrategy;
import in.wynk.sms.config.factory.MultiClientSmscConnectionFactoryBean;
import in.wynk.sms.config.properties.MultiClientSmppProperties;
import in.wynk.sms.constants.SMSBeanConstant;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.receiver.DefaultDeliveryReportConsumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "wynk.saas.smpp", name = "enabled", havingValue = "true")
public class MultiClientSmppSenderBeanRegisterer implements EnvironmentAware, BeanDefinitionRegistryPostProcessor {

    @Setter
    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        final BindResult<MultiClientSmppProperties> result = Binder.get(environment).bind("wynk.saas.smpp", MultiClientSmppProperties.class);
        final MultiClientSmppProperties properties = result.get();
        if (properties.isEnabled()) {
            log.info(SmsLoggingMarkers.MULTI_SMPP_BEAN_DEFINITION_REGISTRATION, "registering multi client smpp bean definition ....");
            for (Map.Entry<String, SmppProperties> clientEntry : properties.getClients().entrySet()) {
                if (clientEntry.getValue().isEnabled()) {
                    final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(StrategySenderManager.class);
                    builder.addConstructorArgReference(clientEntry.getKey() + SMSBeanConstant.SMSC_CONNECTION_HOLDER_BEAN);
                    builder.addConstructorArgValue(RoutingStrategy.getRoutingStrategy(clientEntry.getValue().getRoutingStrategy()));
                    registry.registerBeanDefinition(clientEntry.getKey() + SMSBeanConstant.SMPP_SENDER_MANAGER_BEAN, builder.getBeanDefinition());
                } else {
                    log.warn(SmsLoggingMarkers.MULTI_SMPP_BEAN_DEFINITION_REGISTRATION, "client {} smpp registration is disabled ....", clientEntry.getKey());
                }
            }
        } else {
            log.warn(SmsLoggingMarkers.MULTI_SMPP_BEAN_DEFINITION_REGISTRATION, "multi client mongo repository factories is disabled ....");
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final BindResult<MultiClientSmppProperties> result = Binder.get(environment).bind("wynk.saas.smpp", MultiClientSmppProperties.class);
        final MultiClientSmppProperties properties = result.get();
        if (properties.isEnabled()) {
            log.info(SmsLoggingMarkers.MULTI_SMPP_BEAN_REFERENCE_RESOLUTION, "resolving multi client smpp bean definition ....");
            for (Map.Entry<String, SmppProperties> clientEntry : properties.getClients().entrySet()) {
                if (clientEntry.getValue().isEnabled()) {
                    beanFactory.registerSingleton(clientEntry.getKey() + SMSBeanConstant.SMSC_CONNECTION_HOLDER_BEAN, new MultiClientSmscConnectionFactoryBean(new AlwaysSuccessSmppResultGenerator(), new ClientFactory(), new DefaultTypeOfAddressParser(), new ConnectionManagerFactory(), Collections.singletonList(new DefaultDeliveryReportConsumer())).getObject(clientEntry.getValue()));
                } else {
                    log.warn(SmsLoggingMarkers.MULTI_SMPP_BEAN_REFERENCE_RESOLUTION, "client {} smpp dependency resolution is disabled for source {} ....", clientEntry.getKey(), clientEntry.getValue());
                }
            }
        } else {
            log.warn(SmsLoggingMarkers.MULTI_SMPP_BEAN_REFERENCE_RESOLUTION, "multi client dependency resolution is disabled ....");
        }
    }
}
