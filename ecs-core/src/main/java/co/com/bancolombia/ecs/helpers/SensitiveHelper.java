package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.helpers.strategy.MaskingStrategy;
import co.com.bancolombia.ecs.helpers.strategy.PartialMaskingStrategy;
import co.com.bancolombia.ecs.helpers.strategy.RemoveMaskingStrategy;
import co.com.bancolombia.ecs.helpers.strategy.FullMaskingStrategy;
import co.com.bancolombia.ecs.helpers.strategy.CustomMaskingStrategy;
import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for filtering sensitive data in JSON content based on predefined rules.
 */
@Log4j2
@UtilityClass
public class SensitiveHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Map<String, List<SensitiveRulesConfig.SensitiveDataRule>> rules = Collections.emptyMap();

    private static final String INIT_MESSAGE = "{} sensitive data rules loaded";
    private static final String NO_SENSITIVE_CONFIGURED = "No sensitive rules configured, returning original content";

    private static final Map<SensitiveRulesConfig.MaskingType, MaskingStrategy> STRATEGIES = Map.of(
            SensitiveRulesConfig.MaskingType.FULL, new FullMaskingStrategy(),
            SensitiveRulesConfig.MaskingType.PARTIAL, new PartialMaskingStrategy(),
            SensitiveRulesConfig.MaskingType.CUSTOM, new CustomMaskingStrategy(),
            SensitiveRulesConfig.MaskingType.REMOVE, new RemoveMaskingStrategy()
    );

    private static final JsonFieldNavigator NAVIGATOR = new JsonFieldNavigator(STRATEGIES);

    /**
     * Initializes the sensitive data rules from the provided map.
     *
     * @param rulesMap A map where the key is a URI pattern and the value is a list of sensitive data rules.
     */
    public static void init(Map<String, List<SensitiveRulesConfig.SensitiveDataRule>> rulesMap) {
        if (rulesMap != null) {
            log.info(INIT_MESSAGE, rulesMap.size());
            rules = rulesMap;
        }
    }

    /**
     * Filters sensitive data from the given JSON content based on the configured rules for the specified URI.
     *
     * @param jsonContent The JSON content as a string.
     * @param uri         The URI to determine which rules to apply.
     * @return The JSON content with sensitive data masked or removed according to the rules.
     */
    public static String filterSensitiveData(String jsonContent, String uri) {
        if (rules.isEmpty() || jsonContent == null || jsonContent.isEmpty()) {
            log.debug(NO_SENSITIVE_CONFIGURED);
            return jsonContent;
        }
        List<SensitiveRulesConfig.SensitiveDataRule> applicableRules = findApplicableRules(uri);
        if (applicableRules.isEmpty()) return jsonContent;

        try {
            JsonNode root = MAPPER.readTree(jsonContent);
            for (SensitiveRulesConfig.SensitiveDataRule rule : applicableRules) {
                for (String fieldPath : rule.getFieldPaths()) {
                    if (fieldPath == null || fieldPath.isBlank()) continue;
                    if (fieldPath.contains(".")) {
                        NAVIGATOR.maskFieldByPath(root, fieldPath, rule);
                    } else {
                        NAVIGATOR.maskFieldByName(root, fieldPath, rule);
                    }
                }
            }
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.warn("Error filtering sensitive data: {}", e.getMessage());
            return jsonContent;
        }
    }

    /**
     * Finds the applicable sensitive data rules for the given URI.
     *
     * @param uri The URI to match against the configured rules.
     * @return A list of sensitive data rules that are enabled and match the URI.
     */
    private static List<SensitiveRulesConfig.SensitiveDataRule> findApplicableRules(String uri) {
        return rules.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getKey(), uri))
                .flatMap(entry -> entry.getValue().stream())
                .filter(SensitiveRulesConfig.SensitiveDataRule::isEnabled)
                .toList();
    }
}
