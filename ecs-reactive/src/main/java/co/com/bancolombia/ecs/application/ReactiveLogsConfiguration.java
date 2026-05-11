package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ReactiveLogsHandler;
import co.com.bancolombia.ecs.application.handler.EcsErrorContextHandler;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.PrintOnErrorProperties;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
public class ReactiveLogsConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(WebFilter.class)
    public ReactiveLogsHandler reactiveLogsHandler(ServiceProperties serviceProps,
                                                   SensitiveRequestProperties requestProps,
                                                   SensitiveResponseProperties responseProps,
                                                   PrintOnErrorProperties printOnErrorProperties,
                                                   MessageIdMngUseCase messageIdMngUseCase) {
        var config = new EcsPropertiesConfig(
                serviceProps, requestProps, responseProps, printOnErrorProperties);
        return new ReactiveLogsHandler(config, messageIdMngUseCase);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public EcsErrorContextHandler ecsErrorContextHandler() {
        return new EcsErrorContextHandler();
    }
}
