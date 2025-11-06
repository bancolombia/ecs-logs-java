package co.com.bancolombia.ecs.helpers.strategy;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;

/**
 * Remove masking strategy for sensitive data.
 * This strategy removes the value entirely, returning null.
 */
public class RemoveMaskingStrategy implements MaskingStrategy {

    /**
     * Removes the given value by returning null.
     *
     * @param value The original value to be removed.
     * @param rule  The sensitive data rule (not used in this strategy).
     * @return null, indicating the value has been removed.
     */
    @Override
    public String mask(String value, SensitiveRulesConfig.SensitiveDataRule rule) {
        return null;
    }
}
