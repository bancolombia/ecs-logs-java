package co.com.bancolombia.ecs.infra.config;


import co.com.bancolombia.ecs.helpers.SensitiveHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Log4j2
@Configuration
public class SensitiveRulesInitializer {

    public SensitiveRulesInitializer(SensitiveRulesConfig sensitiveConfig) {

        log.info("Initializing sensitive rules with config: {}", sensitiveConfig.getSensitiveData());
        Map<String, List<SensitiveRulesConfig.SensitiveDataRule>> sensitiveRules =
                buildSensitiveDataRulesMap(sensitiveConfig.getSensitiveData());
        log.info("Built {} rule groups", sensitiveRules.size());

        sensitiveRules.forEach((pattern, rulesList) -> {
            log.info("Pattern: '{}' with {} rules", pattern, rulesList.size());
            rulesList.forEach(rule -> log.info("  Rule enabled: {} for fields: {}",
                    rule.isEnabled(), String.join(",", rule.getFieldPaths())));
        });

        SensitiveHelper.init(sensitiveRules);
    }

    private Map<String, List<SensitiveRulesConfig.SensitiveDataRule>> buildSensitiveDataRulesMap(String rulesJson) {
        if (rulesJson == null || rulesJson.isBlank()) {
            return Map.of();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<SensitiveRulesConfig.SensitiveDataRule> rules = mapper.readValue(
                    rulesJson,
                    new TypeReference<List<SensitiveRulesConfig.SensitiveDataRule>>() {}
            );

            return rules.stream()
                    .filter(SensitiveRulesConfig.SensitiveDataRule::isEnabled)
                    .collect(Collectors.groupingBy(SensitiveRulesConfig.SensitiveDataRule::getUriPattern));

        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing sensitive data rules", e);
        }
    }

}
