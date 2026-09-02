package co.com.bancolombia.ecs.model.helpers;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonBlockingIdGeneratorTest {

    private static final String RFC_4122_V4 =
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    @Test
    void shouldGenerateValidRfc4122Version4Uuid() {
        for (int i = 0; i < 1000; i++) {
            String id = NonBlockingIdGenerator.randomUuid();
            assertTrue(id.matches(RFC_4122_V4), "no es un UUID v4 valido: " + id);
        }
    }

    @Test
    void shouldBeParseableByJavaUuid() {
        String id = NonBlockingIdGenerator.randomUuid();
        assertDoesNotThrow(() -> UUID.fromString(id));
        assertEquals(4, UUID.fromString(id).version());
        assertEquals(2, UUID.fromString(id).variant());
    }

    @Test
    void shouldGenerateUniqueIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            ids.add(NonBlockingIdGenerator.randomUuid());
        }
        assertEquals(10000, ids.size());
    }
}
