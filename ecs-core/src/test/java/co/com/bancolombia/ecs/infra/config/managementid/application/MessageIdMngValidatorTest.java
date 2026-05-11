package co.com.bancolombia.ecs.infra.config.managementid.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanInitializationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageIdMngValidatorTest {

    private static final String PROPERTY_KEY =
            "adapter.ecs.logs.message-id.enable_auto_register_message_id";

    // ── Valores inválidos ─────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"1", "0", "yes", "no", "on", "off", "maybe", "2", "si", "verdadero"})
    void shouldThrowWhenValueIsNotStrictlyTrueOrFalse(String invalidValue) {
        BeanInitializationException ex = assertThrows(
                BeanInitializationException.class,
                () -> MessageIdMngValidator.validate(invalidValue, PROPERTY_KEY)
        );
        assertTrue(ex.getMessage().contains(invalidValue),
                "El mensaje de error debe incluir el valor inválido recibido");
        assertTrue(ex.getMessage().contains("true") && ex.getMessage().contains("false"),
                "El mensaje de error debe indicar los valores aceptados");
    }

    // ── Valores válidos ───────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "TRUE", "FALSE", "True", "False"})
    void shouldAcceptTrueOrFalseInAnyCase(String validValue) {
        assertDoesNotThrow(
                () -> MessageIdMngValidator.validate(validValue, PROPERTY_KEY),
                "El valor '" + validValue + "' debe ser aceptado sin excepción"
        );
    }

    @Test
    void shouldAcceptNullValue() {
        // null = propiedad no configurada → feature desactivada, no debe lanzar excepción
        assertDoesNotThrow(
                () -> MessageIdMngValidator.validate(null, PROPERTY_KEY),
                "null debe ser aceptado (feature desactivada)"
        );
    }
}
