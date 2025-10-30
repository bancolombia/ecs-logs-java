package co.com.bancolombia.ecs.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class DataSanitizer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String sanitize(String body, Set<String> sensitiveFields, List<Pattern> patterns,
                                  String replacement) {
        if (body == null || body.isEmpty()) return body;

        String sanitizedByRegex = sanitizeJsonWithRegex(body, patterns, replacement);

        try {
            Object parsed = objectMapper.readValue(sanitizedByRegex, new TypeReference<>() {
            });
            if (parsed instanceof Map) {
                sanitizeJsonMap((Map<String, Object>) parsed, toLowerSet(sensitiveFields), replacement);
            }
            return objectMapper.writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            return body;
        }
    }

    public static Map<String, String> sanitizeHeaders(
        Set<Map.Entry<String, List<String>>> requestHeaders, Set<String> allowedHeaders) {

        Set<String> allowedLower = allowedHeaders.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        return requestHeaders.stream()
            .filter(entry -> {
                String lowerKey = entry.getKey().toLowerCase();
                List<String> values = entry.getValue();
                return allowedLower.contains(lowerKey) && !values.isEmpty();
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get(0)
            ));
    }

    private static void sanitizeJsonMap(Map<String, Object> map, Set<String> sensitiveFields, String replacement) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (sensitiveFields.contains(key.toLowerCase())) {
                map.put(key, replacement);
            } else if (value instanceof Map) {
                sanitizeJsonMap((Map<String, Object>) value, sensitiveFields, replacement);
            } else if (value instanceof List) {
                sanitizeJsonList((List<Object>) value, sensitiveFields, replacement);
            } else if (value instanceof String) {
                map.put(key, value);
            }
        }
    }

    private static String sanitizeJsonWithRegex(String rawJson, List<Pattern> patterns, String replacement) {
        String sanitized = rawJson;
        for (Pattern pattern : patterns) {
            var matcher = pattern.matcher(sanitized);
            sanitized = matcher.replaceAll(replacement);
        }
        return sanitized;
    }

    private static void sanitizeJsonList(List<Object> list, Set<String> sensitiveFields, String replacement) {
        for (var i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof Map) {
                sanitizeJsonMap((Map<String, Object>) item, sensitiveFields, replacement);
            } else if (item instanceof List) {
                sanitizeJsonList((List<Object>) item, sensitiveFields, replacement);
            } else if (item instanceof String) {
                list.set(i, item);
            }
        }
    }

    private static Set<String> toLowerSet(Set<String> input) {
        Set<String> result = new HashSet<>();
        for (String item : input) {
            result.add(item.toLowerCase());
        }
        return result;
    }
}