package co.com.bancolombia.ecs.infra.config.managementid.application;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * Validates that the raw value of {@code enable_auto_register_message_id}
 * is strictly {@code "true"} or {@code "false"}.
 *
 * <p>Spring Boot silently converts values like {@code "yes"}, {@code "on"},
 * {@code "1"} to {@code Boolean.TRUE} when the property type is {@code Boolean}.
 * Using {@code String} as the property type and calling this validator at startup
 * produces a clear error, as close as possible to a compile-time check.
 */
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

    /**
     * Validates {@code rawValue} against the allowed domain {"true", "false"}.
     *
     * @param rawValue    the raw string read from the YAML property
     * @param propertyKey the canonical property key (for the error message)
     * @throws BeanInitializationException if the value is non-null and not accepted
     */
    public static void validate(String rawValue, String propertyKey) {
        if (rawValue != null && !VALID_VALUES.contains(rawValue.toLowerCase())) {
            throw new BeanInitializationException(
                    String.format(ERROR_VALUE_MSG, rawValue, propertyKey));
        }
    }

    /**
     * Scans all enumerable property sources for keys that start with the
     * {@code adapter.ecs.logs.message-id} namespace but are not the recognized
     * canonical key. This catches prefix typos such as {@code message-id-:}.
     *
     * @param environment the application environment
     * @throws BeanInitializationException if an unknown key is found
     */
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
