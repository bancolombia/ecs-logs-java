package co.com.bancolombia.ecs.helpers.strategy;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;

/**
 * Custom masking strategy for sensitive data.
 * Allows applying a user-defined mask to a specific value.
 * If no custom mask is defined, returns the original value.
 */
public class CustomMaskingStrategy implements MaskingStrategy {

    /**
     * Masks the given value using a custom mask defined in the rule.
     * If no custom mask is provided, returns the original value.
     *
     * @param value The original value to be masked.
     * @param rule  The sensitive data rule containing the custom mask.
     * @return The masked value or the original value if no custom mask is defined.
     */
    @Override
    public String mask(String value, SensitiveRulesConfig.SensitiveDataRule rule) {
        if (value == null) {
            return null;
        }
        String custom = rule.getCustomMask();
        return (custom != null) ? custom : value;
    }
}