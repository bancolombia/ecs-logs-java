package co.com.bancolombia.ecs.infra.config.managementid.domain;

import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngValidator;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

@ConfigurationProperties(prefix = "adapter.ecs.logs.message-id")
public class MessageIdRequestProperties implements InitializingBean, EnvironmentAware {

    private static final String PROPERTY_KEY =
            "adapter.ecs.logs.message-id.enable_auto_register_message_id";

    private final String enableAutoRegisterMessageId;
    private Environment environment;

    @Getter
    private Boolean enabled;

    public MessageIdRequestProperties(String enableAutoRegisterMessageId) {
        this.enableAutoRegisterMessageId = enableAutoRegisterMessageId;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        MessageIdMngValidator.validatePropertyKeys(environment);
        MessageIdMngValidator.validate(enableAutoRegisterMessageId, PROPERTY_KEY);
        this.enabled = (enableAutoRegisterMessageId == null || enableAutoRegisterMessageId.isBlank())
                ? null
                : Boolean.parseBoolean(enableAutoRegisterMessageId.toLowerCase());
    }
}
