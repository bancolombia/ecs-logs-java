package co.com.bancolombia.ecs.infra.config.managementid.application;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.Set;


public final class MessageIdMngValidator {

    static final String MESSAGE_ID_PREFIX = "adapter.ecs.logs.message-id";

    private static final Set<String> VALID_KEYS = Set.of(
            "adapter.ecs.logs.message-id.enable_auto_register_message_id",
            "adapter.ecs.logs.message-id.enable-auto-register-message-id",
            "adapter.ecs.logs.message-id.enableautoregistermessageid"
    );

    private static final Set<String> VALID_VALUES = Set.of("true", "false");

    private static final String ERROR_VALUE_MSG =
            "Invalid value '%s' for property '%s'. " +
                    "Accepted values are: 'true' or 'false'. " +
                    "Please review your application.yaml configuration.";

    private static final String ERROR_KEY_MSG =
            "Invalid or unknown property '%s' under the 'adapter.ecs.logs.message-id' namespace. " +
                    "The only accepted property is " +
                    "'adapter.ecs.logs.message-id.enable_auto_register_message_id'. " +
                    "Check for typos in the property key (e.g. 'message-id-' instead of 'message-id'). " +
                    "Please review your application.yaml configuration.";

    private MessageIdMngValidator() {}


    public static void validate(String rawValue, String propertyKey) {
        if (rawValue != null && !rawValue.isBlank() && !VALID_VALUES.contains(rawValue.toLowerCase())) {
            throw new BeanInitializationException(
                    String.format(ERROR_VALUE_MSG, rawValue, propertyKey));
        }
    }

    public static void validatePropertyKeys(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment ce)) return;
        for (var source : ce.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> ep) {
                for (String key : ep.getPropertyNames()) {
                    if (key.toLowerCase().startsWith(MESSAGE_ID_PREFIX)
                            && !VALID_KEYS.contains(key.toLowerCase())) {
                        throw new BeanInitializationException(
                                String.format(ERROR_KEY_MSG, key));
                    }
                }
            }
        }
    }
}
