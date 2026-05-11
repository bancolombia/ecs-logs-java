package co.com.bancolombia.ecs.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSanitizerTest {

    @Test
    void testSanitizeWithSensitiveFieldsShouldReplaceValues() {
        String json = "{\"password\":\"1234\",\"name\":\"test\"}";
        Set<String> sensitiveFields = Set.of("password");
        List<Pattern> patterns = List.of();
        String replacement = "****";

        String sanitized = DataSanitizer.sanitize(json, sensitiveFields, patterns, replacement);

        assertTrue(sanitized.contains("\"password\":\"****\""));
        assertTrue(sanitized.contains("\"name\":\"test\""));
    }

    @Test
    void testSanitizeWithRegexPatternShouldReplaceMatches() {
        String json = "{\"card\":\"4111111111111111\",\"name\":\"test\"}";
        Set<String> sensitiveFields = Set.of();
        List<Pattern> patterns = List.of(Pattern.compile("4111\\d{12}"));
        String replacement = "xxxx";

        String sanitized = DataSanitizer.sanitize(json, sensitiveFields, patterns, replacement);

        assertTrue(sanitized.contains("xxxx"));
    }

    @Test
    void testSanitizeJsonListWithShouldReplaceMatches() {
        String jsonList = """
            {
                "data": [
                  { "card": "4111111111111111", "name": "John" },
                  { "card": "4111222233334444", "name": "Jane" }
                ],
                "list": [
                  "type"
                ]
            }
            """;

        Set<String> sensitiveFields = Set.of();
        List<Pattern> patterns = List.of();
        String replacement = "xxxx";

        String sanitized = DataSanitizer.sanitize(jsonList, sensitiveFields, patterns, replacement);

        assertTrue(sanitized.contains("4111111111111111"));
        assertFalse(sanitized.contains("xxxx"));
        assertTrue(sanitized.contains("Jane"));
    }


    @Test
    void testSanitizeHeadersShouldFilterAllowedHeaders() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("Authorization", List.of("Bearer token")),
            Map.entry("X-Custom", List.of("123"))
        );
        Set<String> allowed = Set.of("Authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("Bearer token", result.get("authorization"));
    }

    @Test
    void testSanitizeInvalidJsonShouldReturnOriginal() {
        String body = "{invalid json}";
        Set<String> sensitiveFields = Set.of("password");
        List<Pattern> patterns = List.of();
        String replacement = "****";

        String sanitized = DataSanitizer.sanitize(body, sensitiveFields, patterns, replacement);

        assertEquals(body, sanitized);
    }

    @Test
    void testSanitizeInvalidBodyShouldReturnOriginal() {
        String body = "";
        Set<String> sensitiveFields = Set.of("password");
        List<Pattern> patterns = List.of();
        String replacement = "****";

        String sanitized = DataSanitizer.sanitize(body, sensitiveFields, patterns, replacement);
        assertEquals(body, sanitized);
        assertNull(DataSanitizer.sanitize(null, sensitiveFields, patterns, replacement));
    }

    @Test
    void testSanitizeStringBodyShouldReturnOriginal() {
        String body = """
                [
                    {
                        "message": "Hello World!"
                    }
                ]
            """;
        Set<String> sensitiveFields = Set.of("password");
        List<Pattern> patterns = List.of();
        String replacement = "****";

        String sanitized = DataSanitizer.sanitize(body, sensitiveFields, patterns, replacement);
        assertNotNull(sanitized);
    }

    @Test
    void testSanitizeListBodyShouldReturnOriginal() {
        String body = """
                {
                    "list": [
                        {
                            "message": "Hello World!",
                            "type": [
                                "1",
                                "2",
                                "3"
                            ]
                        },
                        "Hello World!",
                        ["string", [12]]
                    ],
                    "map": {
                        "data": {
                            "name": "John",
                            "age": 12
                        }
                    }
                }
            """;
        Set<String> sensitiveFields = Set.of("password");
        List<Pattern> patterns = List.of();
        String replacement = "****";

        String sanitized = DataSanitizer.sanitize(body, sensitiveFields, patterns, replacement);
        assertNotNull(sanitized);
    }

    @Test
    void testSanitizeHeadersShouldNoFilterAllowedHeaders() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("Authorization", List.of("Bearer token")),
            Map.entry("X-Custom", List.of("123"))
        );
        Set<String> allowed = Set.of("AID");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(0, result.size());
        assertNull(result.get("Authorization"));
    }

    @Test
    void testSanitizeHeadersShouldValuesEmptyAllowedHeaders() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("Authorization", List.of()),
            Map.entry("X-Custom", List.of())
        );
        Set<String> allowed = Set.of("Authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(0, result.size());
        assertNull(result.get("Authorization"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"message-id", "Message-Id", "MESSAGE-ID", "message_id", "Message_Id",
        "MessageId", "messageid", "MESSAGEID", "MESSAGE_ID"})
    void testSanitizeHeadersShouldMatchMessageIdRegardlessOfCasing(String headerKey) {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry(headerKey, List.of("app-uuid-123"))
        );
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("app-uuid-123", result.get("message-id"));
    }

    @Test
    void testSanitizeHeadersShouldOutputCanonicalLowercaseKeys() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("Consumer-Acronym", List.of("ABC")),
            Map.entry("Message-Id", List.of("uuid-1")),
            Map.entry("X-Channel", List.of("web"))
        );
        Set<String> allowed = Set.of("consumer-acronym", "message-id", "x-channel");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(3, result.size());
        assertEquals("ABC", result.get("consumer-acronym"));
        assertEquals("uuid-1", result.get("message-id"));
        assertEquals("web", result.get("x-channel"));
        assertNull(result.get("Consumer-Acronym"));
        assertNull(result.get("Message-Id"));
        assertNull(result.get("X-Channel"));
    }

    @Test
    void testSanitizeHeadersShouldMatchUnderscoreVariant() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("consumer_acronym", List.of("XYZ"))
        );
        Set<String> allowed = Set.of("consumer-acronym");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("XYZ", result.get("consumer-acronym"));
    }

    @Test
    void testSanitizeHeadersShouldMatchCamelCaseVariant() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("consumerAcronym", List.of("DEF"))
        );
        Set<String> allowed = Set.of("consumer-acronym");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("DEF", result.get("consumer-acronym"));
    }

    @Test
    void testSanitizeHeadersShouldNotMatchOtherSymbolsVariant() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
                Map.entry("message.id", List.of("uuid-1"))
        );
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeHeadersShouldMatchOtherSymbolsIfAllowedVariant() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
                Map.entry("message.id", List.of("uuid-1"))
        );
        Set<String> allowed = Set.of("message.id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("uuid-1", result.get("message.id"));
    }

    @Test
    void testSanitizeHeadersShouldMatchMultipleAllowedHeadersWithMixedCasing() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("Message_Id", List.of("id-1")),
            Map.entry("AUTHORIZATION", List.of("Bearer tok")),
            Map.entry("X-Not-Allowed", List.of("nope"))
        );
        Set<String> allowed = Set.of("message-id", "authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(2, result.size());
        assertEquals("id-1", result.get("message-id"));
        assertEquals("Bearer tok", result.get("authorization"));
        assertNull(result.get("x-not-allowed"));
    }

    @Test
    void testSanitizeHeadersShouldReturnEmptyWhenNoHeadersMatchAllowList() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("X-Custom", List.of("val1")),
            Map.entry("X-Other", List.of("val2"))
        );
        Set<String> allowed = Set.of("message-id", "authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeHeadersShouldSkipHeadersWithEmptyValuesList() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("message-id", List.of()),
            Map.entry("authorization", List.of("Bearer token"))
        );
        Set<String> allowed = Set.of("message-id", "authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertNull(result.get("message-id"));
        assertEquals("Bearer token", result.get("authorization"));
    }

    @Test
    void testSanitizeHeadersShouldReturnEmptyWhenAllowedSetIsEmpty() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("message-id", List.of("uuid-1")),
            Map.entry("authorization", List.of("Bearer token"))
        );
        Set<String> allowed = Set.of();

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeHeadersShouldReturnEmptyWhenRequestHeadersAreEmpty() {
        Set<Map.Entry<String, List<String>>> headers = Set.of();
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeHeadersShouldKeepFirstValueWhenDuplicateNormalizedKeys() {
        // Use LinkedHashSet to control iteration order
        Set<Map.Entry<String, List<String>>> headers = new LinkedHashSet<>();
        headers.add(Map.entry("message-id", List.of("first-value")));
        headers.add(Map.entry("Message_Id", List.of("second-value")));
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("first-value", result.get("message-id"));
    }

    @Test
    void testSanitizeHeadersShouldUseFirstValueFromMultiValueHeader() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("message-id", List.of("first", "second", "third"))
        );
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("first", result.get("message-id"));
    }

    @Test
    void testSanitizeHeadersShouldHandleAllowListWithMixedCasing() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("message-id", List.of("uuid-1"))
        );
        Set<String> allowed = Set.of("Message-Id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertEquals(1, result.size());
        assertEquals("uuid-1", result.get("message-id"));
    }

    @Test
    void testSanitizeHeadersShouldHandleHeaderKeyWithOnlySeparators() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("--__--", List.of("value"))
        );
        Set<String> allowed = Set.of("authorization");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSanitizeHeadersShouldNotMatchPartialKeys() {
        Set<Map.Entry<String, List<String>>> headers = Set.of(
            Map.entry("message-id-extra", List.of("val1")),
            Map.entry("xmessage-id", List.of("val2"))
        );
        Set<String> allowed = Set.of("message-id");

        Map<String, String> result = DataSanitizer.sanitizeHeaders(headers, allowed);

        assertTrue(result.isEmpty());
    }
}