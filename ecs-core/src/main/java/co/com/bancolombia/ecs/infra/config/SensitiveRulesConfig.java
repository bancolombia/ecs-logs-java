package co.com.bancolombia.ecs.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "adapter.ecs.logs.sensitive-rules")
public class SensitiveRulesConfig {

    private String sensitiveData;

    @Data
    public static class SensitiveDataRule {
        private String uriPattern;
        private String[] fieldPaths;
        private MaskingType maskingType = MaskingType.PARTIAL;
        private String customMask;
        private boolean enabled = true;
        private String maskingChar = "*";
        private double visibilityPercentage = 0.2;
    }

    public enum MaskingType {
        FULL,
        PARTIAL,
        CUSTOM,
        REMOVE
    }
}
