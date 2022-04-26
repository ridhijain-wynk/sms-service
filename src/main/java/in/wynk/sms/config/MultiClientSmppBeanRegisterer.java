package in.wynk.sms.config;

import in.wynk.smpp.config.properties.SmppProperties;
import in.wynk.sms.config.properties.MultiClientSmppProperties;
import in.wynk.sms.constants.SmsLoggingMarkers;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "wynk.saas.smpp", name = "enabled", havingValue = "true")
public class MultiClientSmppBeanRegisterer implements EnvironmentAware, BeanDefinitionRegistryPostProcessor {

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
                    for (Map.Entry<String, SmppProperties.SMSC> sourceEntry : clientEntry.getValue().getConnections().entrySet()) {
                        if (sourceEntry.getValue().isEnabled()) {
                            // register bean definition
                        } else {
                            log.warn(SmsLoggingMarkers.MULTI_SMPP_BEAN_DEFINITION_REGISTRATION, "client {} smpp registration is disabled for source {} ....", clientEntry.getKey(), sourceEntry.getKey());
                        }
                    }
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
        // resolve all the lazy references such has SessionConnectionHolder
    }

}
