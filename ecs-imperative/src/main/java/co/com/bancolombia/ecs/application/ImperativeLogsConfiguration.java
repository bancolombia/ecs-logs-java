package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ImperativeLogsHandler;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ImperativeLogsConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(Filter.class)
    public ImperativeLogsHandler imperativeLogsHandler(
        ServiceProperties serviceProps,
        SensitiveRequestProperties requestProps,
        SensitiveResponseProperties responseProps) {
        EcsPropertiesConfig config = new EcsPropertiesConfig(serviceProps, requestProps, responseProps);
        return new ImperativeLogsHandler(config);
    }
}
