package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrintOnErrorPropertiesTest {

    @Test
    void shouldSetAndGetProperties() {
        PrintOnErrorProperties props = new PrintOnErrorProperties();

        props.setPrintReqResp(Boolean.TRUE);
        props.setPrintReqRespLevel("Throwable");

        assertEquals(Boolean.TRUE, props.getPrintReqResp());
        assertEquals(ExceptionLevel.THROWABLE, props.getPrintReqRespLevel());
    }

    @Test
    void shouldSetAndGetPropertiesIgnoringCase() {
        PrintOnErrorProperties props = new PrintOnErrorProperties();

        props.setPrintReqResp(Boolean.TRUE);
        props.setPrintReqRespLevel("businessexceptionecs");

        assertEquals(Boolean.TRUE, props.getPrintReqResp());
        assertEquals(ExceptionLevel.BUSINESS_EXCEPTION_ECS, props.getPrintReqRespLevel());
    }

    @Test
    void shouldAllowNullDefaults() {
        PrintOnErrorProperties props = new PrintOnErrorProperties();

        assertNull(props.getPrintReqResp());
        assertNull(props.getPrintReqRespLevel());
    }

    @Test
    void shouldFailWhenLevelIsInvalid() {
        PrintOnErrorProperties props = new PrintOnErrorProperties();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> props.setPrintReqRespLevel("OtroNivel")
        );

        System.out.println(exception.getMessage());

        assertTrue(exception.getMessage().contains("Valor no permitido"));
        assertTrue(exception.getMessage().contains("OtroNivel"));
        assertTrue(exception.getMessage().contains("Valores permitidos: BusinessExceptionECS, Exception, Throwable"));
    }

    @Test
    void shouldMatchBusinessExceptionEcsOnlyWhenConfiguredAsBusinessException() {
        Throwable businessException = new BusinessExceptionECS("error");
        Throwable genericException = new RuntimeException("error");
        Throwable throwable = new Throwable("error");

        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                businessException,
                ExceptionLevel.BUSINESS_EXCEPTION_ECS));
        assertFalse(PrintOnErrorProperties.matchesConfiguredLevel(
                genericException,
                ExceptionLevel.BUSINESS_EXCEPTION_ECS));
        assertFalse(PrintOnErrorProperties.matchesConfiguredLevel(
                throwable,
                ExceptionLevel.BUSINESS_EXCEPTION_ECS));
    }

    @Test
    void shouldMatchAnyExceptionWhenConfiguredAsException() {
        Throwable businessException = new BusinessExceptionECS("error");
        Throwable genericException = new RuntimeException("error");
        Throwable throwable = new Throwable("error");

        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                businessException,
                ExceptionLevel.EXCEPTION));
        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                genericException,
                ExceptionLevel.EXCEPTION));
        assertFalse(PrintOnErrorProperties.matchesConfiguredLevel(
                throwable,
                ExceptionLevel.EXCEPTION));
    }

    @Test
    void shouldMatchAnyThrowableWhenConfiguredAsThrowable() {
        Throwable businessException = new BusinessExceptionECS("error");
        Throwable genericException = new RuntimeException("error");
        Throwable throwable = new Throwable("error");

        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                businessException,
                ExceptionLevel.THROWABLE));
        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                genericException,
                ExceptionLevel.THROWABLE));
        assertTrue(PrintOnErrorProperties.matchesConfiguredLevel(
                throwable,
                ExceptionLevel.THROWABLE));
    }
}
