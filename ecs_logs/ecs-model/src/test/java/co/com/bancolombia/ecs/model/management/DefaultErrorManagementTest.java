package co.com.bancolombia.ecs.model.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultErrorManagementTest {

    private DefaultErrorManagement errorManagement;

    @BeforeEach
    void setUp() {
        errorManagement = new DefaultErrorManagement();
    }

    @Test
    void shouldReturnDefaultStatus() {
        int status = errorManagement.getStatus();

        assertEquals(500, status);
    }

    @Test
    void shouldReturnDefaultMessage() {
        String message = errorManagement.getMessage();

        assertEquals("Ha ocurrido un error interno en el servicio.", message);
    }

    @Test
    void shouldReturnDefaultErrorCode() {
        String errorCode = errorManagement.getErrorCode();

        assertEquals("ER500-01", errorCode);
    }

    @Test
    void shouldReturnEmptyInternalMessage() {
        String internalMessage = errorManagement.getInternalMessage();

        assertNotNull(internalMessage);
        assertTrue(internalMessage.isEmpty());
    }

    @Test
    void shouldReturnEmptyLogCode() {
        String logCode = errorManagement.getLogCode();

        assertNotNull(logCode);
        assertTrue(logCode.isEmpty());
    }
}

