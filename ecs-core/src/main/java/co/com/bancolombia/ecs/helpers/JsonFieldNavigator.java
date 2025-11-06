package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.domain.model.PathContext;
import co.com.bancolombia.ecs.helpers.strategy.MaskingStrategy;
import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Utility class for navigating and masking fields in a JSON structure
 * based on specified paths and rules.
 */
public class JsonFieldNavigator {

    private final Map<SensitiveRulesConfig.MaskingType, MaskingStrategy> strategies;

    /**
     * Constructs a JsonFieldNavigator with the provided masking strategies.
     *
     * @param strategies A map of masking types to their corresponding strategies.
     */
    public JsonFieldNavigator(Map<SensitiveRulesConfig.MaskingType, MaskingStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Masks a field in the JSON structure based on the specified path and rule.
     *
     * @param rootNode  The root JSON node to start from.
     * @param fieldPath The dot-separated path to the target field (supports arrays with [*]).
     * @param rule      The sensitive data rule to apply for masking.
     */
    public void maskFieldByPath(JsonNode rootNode, String fieldPath, SensitiveRulesConfig.SensitiveDataRule rule) {
        if (rootNode == null || fieldPath == null || fieldPath.isBlank()) return;
        String[] pathParts = fieldPath.split("\\.");
        var context = new PathContext(pathParts, 0, rule);
        processPathPart(rootNode, context);
    }

    /**
     * Masks all occurrences of a field with the specified name in the JSON structure.
     *
     * @param rootNode  The root JSON node to start from.
     * @param fieldName The name of the field to mask.
     * @param rule      The sensitive data rule to apply for masking.
     */
    public void maskFieldByName(JsonNode rootNode, String fieldName, SensitiveRulesConfig.SensitiveDataRule rule) {
        if (rootNode == null || fieldName == null || fieldName.isBlank()) return;

        if (rootNode.isObject()) {
            var objectNode = (ObjectNode) rootNode;
            if (objectNode.has(fieldName) && objectNode.get(fieldName).isTextual()) {
                applyMask(objectNode, fieldName, objectNode.get(fieldName).asText(), rule);
            }
            objectNode.properties().forEach(entry -> maskFieldByName(entry.getValue(), fieldName, rule));
        } else if (rootNode.isArray()) {
            for (JsonNode element : rootNode) {
                maskFieldByName(element, fieldName, rule);
            }
        }
    }

    /**
     * Processes a part of the path in the JSON structure, handling both single fields and arrays.
     *
     * @param currentNode The current JSON node being processed.
     * @param context     The context containing path parts and the current index.
     */
    private void processPathPart(JsonNode currentNode, PathContext context) {
        if (currentNode == null || context == null) return;
        String pathPart = context.getPathParts()[context.getCurrentIndex()];
        if (pathPart.contains("[*]")) {
            processArrayPathPart(currentNode, pathPart, context);
        } else {
            processSinglePathPart(currentNode, pathPart, context);
        }
    }

    /**
     * Processes a path part that indicates an array, applying the rule to each element.
     *
     * @param currentNode The current JSON node being processed.
     * @param pathPart    The path part indicating an array (e.g., "items[*]").
     * @param context     The context containing path parts and the current index.
     */
    private void processArrayPathPart(JsonNode currentNode, String pathPart, PathContext context) {
        if (!(currentNode instanceof ObjectNode objectNode)) return;
        String fieldName = pathPart.replace("[*]", "");
        JsonNode arrayNode = objectNode.get(fieldName);
        if (arrayNode == null || !arrayNode.isArray()) return;

        if (context.getCurrentIndex() + 1 >= context.getPathParts().length) {
            maskArrayElements((ArrayNode) arrayNode, context.getRule());
        } else {
            PathContext next = context.next();
            for (JsonNode element : arrayNode) {
                processPathPart(element, next);
            }
        }
    }

    /**
     * Masks elements of an array node based on the specified rule.
     *
     * @param arrayNode The array node containing elements to be masked.
     * @param rule      The sensitive data rule to apply for masking.
     */
    private void maskArrayElements(ArrayNode arrayNode, SensitiveRulesConfig.SensitiveDataRule rule) {
        for (var i = 0; i < arrayNode.size(); i++) {
            JsonNode element = arrayNode.get(i);
            if (element != null && element.isTextual()) {
                MaskingStrategy strategy = strategies.get(rule.getMaskingType());
                String masked = strategy.mask(element.asText(), rule);
                if (masked == null) {
                    arrayNode.remove(i);
                } else {
                    arrayNode.set(i, arrayNode.textNode(masked));
                }
            }
        }
    }

    /**
     * Processes a single path part in the JSON structure, navigating to the next node or applying the mask.
     *
     * @param currentNode The current JSON node being processed.
     * @param fieldName   The name of the field to process.
     * @param context     The context containing path parts and the current index.
     */
    private void processSinglePathPart(JsonNode currentNode, String fieldName, PathContext context) {
        if (!(currentNode instanceof ObjectNode objectNode)) return;

        if (context.getCurrentIndex() + 1 >= context.getPathParts().length) {
            applyMaskToTargetField(objectNode, fieldName, context.getRule());
        } else {
            JsonNode next = objectNode.get(fieldName);
            if (next != null) { processPathPart(next, context.next()); }
        }
    }

    /**
     * Applies the masking rule to a target field within an ObjectNode if it exists and is textual.
     *
     * @param objectNode The ObjectNode containing the target field.
     * @param fieldName  The name of the target field to mask.
     * @param rule       The sensitive data rule to apply for masking.
     */
    private void applyMaskToTargetField(ObjectNode objectNode, String fieldName,
                                        SensitiveRulesConfig.SensitiveDataRule rule) {
        if (!objectNode.has(fieldName)) return;
        JsonNode target = objectNode.get(fieldName);
        if (target != null && target.isTextual()) {
            applyMask(objectNode, fieldName, target.asText(), rule);
        }
    }

    /**
     * Applies the specified masking strategy to a field in an ObjectNode.
     * If the strategy returns null, the field is removed; otherwise, it is updated with the masked value.
     *
     * @param parent    The ObjectNode containing the field to be masked.
     * @param fieldName The name of the field to be masked.
     * @param original  The original value of the field.
     * @param rule      The sensitive data rule to apply for masking.
     */
    private void applyMask(ObjectNode parent, String fieldName, String original,
                           SensitiveRulesConfig.SensitiveDataRule rule) {
        MaskingStrategy strategy = strategies.get(rule.getMaskingType());
        if (strategy == null) return;
        String masked = strategy.mask(original, rule);
        if (masked == null) {
            parent.remove(fieldName);
        } else {
            parent.put(fieldName, masked);
        }
    }
}
