package co.com.bancolombia.ecs.infra.config.managementid.domain;

import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngValidator;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Configuration properties for the message-id feature.
 *
 * <p>The field type is intentionally {@code String} (not {@code Boolean}) to capture
 * the raw YAML value before Spring Boot's relaxed binding can silently convert
 * ambiguous values like {@code "yes"}, {@code "on"}, or {@code "1"} to {@code Boolean.TRUE}.
 * {@link MessageIdMngValidator} enforces that only {@code "true"} or {@code "false"} are accepted.
 *
 * <p>Accepted YAML key: {@code enable_auto_register_message_id} (snake_case canonical form).
 * Spring's relaxed binding also accepts kebab-case for backwards compatibility.
 *
 * <p>Semantics of the parsed {@link #enabled} value:
 * <ul>
 *   <li>{@code null}  – feature disabled; library behaves as before</li>
 *   <li>{@code false} – generates a UUID only when a BusinessException has no messageId
 *                       (exception traceability)</li>
 *   <li>{@code true}  – generates a UUID on every request that lacks a message-id header</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "adapter.ecs.logs.message-id")
public class MessageIdRequestProperties implements InitializingBean, EnvironmentAware {

    private static final String PROPERTY_KEY =
            "adapter.ecs.logs.message-id.enable_auto_register_message_id";

    private final String enableAutoRegisterMessageId;
    private Environment environment;

    /** Validated and parsed value; available after {@link #afterPropertiesSet()}. */
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
        this.enabled = enableAutoRegisterMessageId == null
                ? null
                : Boolean.parseBoolean(enableAutoRegisterMessageId.toLowerCase());
    }
}
