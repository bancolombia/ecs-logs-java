package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ImperativeLogsHandler;
import co.com.bancolombia.ecs.infra.config.PrintOnErrorProperties;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.managementid.domain.MessageIdRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImperativeLogsConfigurationTest {

    @Test
    void shouldCreateImperativeLogsHandlerBean() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setName("test-service");

        SensitiveRequestProperties requestProperties = new SensitiveRequestProperties();
        requestProperties.setShow(Boolean.FALSE);

        SensitiveResponseProperties responseProperties = new SensitiveResponseProperties();
        responseProperties.setShow(Boolean.FALSE);

        PrintOnErrorProperties printOnErrorProperties = new PrintOnErrorProperties();

        MessageIdMngUseCase messageIdMngUseCase = new MessageIdMngUseCase(new MessageIdRequestProperties(null));

        ImperativeLogsConfiguration configuration = new ImperativeLogsConfiguration();
        ImperativeLogsHandler handler = configuration.imperativeLogsHandler(
                serviceProperties, requestProperties, responseProperties, printOnErrorProperties, messageIdMngUseCase);

        assertNotNull(handler);
    }
}
