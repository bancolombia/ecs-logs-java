package co.com.bancolombia.ecs.helpers.strategy;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;

/**
 * Strategy interface for masking sensitive data.
 */
public interface MaskingStrategy {

    /**
     * Masks the given value based on the provided sensitive data rule.
     *
     * @param value The original value to be masked.
     * @param rule  The sensitive data rule containing masking parameters.
     * @return The masked value.
     */
    String mask(String value, SensitiveRulesConfig.SensitiveDataRule rule);
}
