package co.com.bancolombia.ecs.infra.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Data
@Component
@ConfigurationProperties(prefix = "adapter.ecs.logs.sampling")
public class SamplingConfig {

    private static final String START_CODE_40X = "40";
    private static final String START_CODE_20X = "20";
    private static final String NAME_RULES_20X = "rules20XJson";
    private static final String NAME_RULES_40X = "rules40XJson";
    private String rules20XJson;
    private String rules40XJson;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SamplingRule> getRules() {
        return Stream.concat(get20XRules().stream(), get40XRules().stream())
                .toList();
    }

    private List<SamplingRule> get20XRules(){
        if (rules20XJson == null || rules20XJson.isBlank()) {
            return Collections.emptyList();
        }
        List<SamplingRule> rules20X;
        try {
            rules20X = objectMapper.readValue(rules20XJson, new TypeReference<List<SamplingRule>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing adapter.ecs.logs.sampling.rules20XJson", e);
        }
        validateRules(START_CODE_20X, rules20X, NAME_RULES_20X);
        return rules20X;
    }

    private List<SamplingRule> get40XRules(){
        if (rules40XJson == null || rules40XJson.isBlank()) {
            return Collections.emptyList();
        }
        List<SamplingRule> rules40X;
        try {
            rules40X = objectMapper.readValue(rules40XJson, new TypeReference<List<SamplingRule>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing adapter.ecs.logs.sampling.rules40XJson", e);
        }
        validateRules(START_CODE_40X, rules40X, NAME_RULES_40X);
        return rules40X;

    }

    private void validateRules(String validResponseCode, List<SamplingRule> rules, String rulesJson) {
        boolean hasInvalid = rules.stream()
                .anyMatch(rule -> !rule.getResponseCode().startsWith(validResponseCode));

        if (hasInvalid) {
            throw new IllegalArgumentException(String.format(
                    "One or more sampling rules in [%s] have an invalid response code. Expected starts with: [%s]",
                    rulesJson, validResponseCode
            ));
        }
    }

    @Data
    public static class SamplingRule {
        private String uri;
        private String responseCode;
        private int showCount;
        private int skipCount;
        private String errorCodes;
    }
}
