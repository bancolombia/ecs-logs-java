package co.com.bancolombia.ecs.helpers.strategy;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;

/**
* Full masking strategy for sensitive data.
* Replaces the original value with a string composed only of the masking character,
* with a defined maximum length.
*/
public class FullMaskingStrategy implements MaskingStrategy {

    private static final int MAX_MASK_LENGTH_FULL = 15;

    /**
     * Masks the given value by replacing it with a string of the masking character,
     * repeated up to a maximum length defined by MAX_MASK_LENGTH_FULL.
     * If the value is null or empty, it returns the original value.
     * @param value The original value to be masked.
     * @param rule The sensitive data rule containing the masking character.
     * @return The masked value.
     */
    @Override
    public String mask(String value, SensitiveRulesConfig.SensitiveDataRule rule) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return rule.getMaskingChar().repeat(MAX_MASK_LENGTH_FULL);
    }
}
