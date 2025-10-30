package co.com.bancolombia.ecs.helpers.strategy;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;

/**
 * Partial masking strategy for sensitive data.
 * Masks a portion of the value while leaving a specified percentage visible.
 */
public class PartialMaskingStrategy implements MaskingStrategy {

    /**
     * Masks the given value by leaving a percentage of characters visible
     * and replacing the rest with a masking character.
     *
     * @param value The original value to be masked.
     * @param rule  The sensitive data rule containing masking parameters.
     * @return The partially masked value.
     */
    @Override
    public String mask(String value, SensitiveRulesConfig.SensitiveDataRule rule) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        if (length == 1) {
            return rule.getMaskingChar();
        }
        double ratio = rule.getVisibilityPercentage();
        int visibleChars = Math.max(1, (int)Math.ceil(length * ratio));
        var visible = value.substring(0, visibleChars);
        String hidden = rule.getMaskingChar().repeat(length - visibleChars);
        return visible + hidden;
    }
}
