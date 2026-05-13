package co.com.bancolombia.ecs.infra.config.managementid.application;

import co.com.bancolombia.ecs.infra.config.managementid.domain.MessageIdRequestProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageIdMngUseCaseTest {

    private static MessageIdRequestProperties propsEnabled() {
        MessageIdRequestProperties p = new MessageIdRequestProperties("true");
        p.afterPropertiesSet();
        return p;
    }

    private static MessageIdRequestProperties propsDisabled() {
        MessageIdRequestProperties p = new MessageIdRequestProperties("false");
        p.afterPropertiesSet();
        return p;
    }

    private final MessageIdMngUseCase useCase = new MessageIdMngUseCase(propsEnabled());


    @Test
    void shouldReturnHeaderValueWhenPresent() {
        assertEquals("header-id-123", useCase.resolveFromRequestEnvironment("header-id-123"));
    }

    @Test
    void shouldGenerateUuidWhenNoHeader() {
        assertNotNull(useCase.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsBlank() {
        assertNotNull(useCase.resolveFromRequestEnvironment("   "));
    }


    @Test
    void shouldUseContextMessageIdFirst() {
        assertEquals("meta-id", MessageIdMngUseCase.resolveForException( "meta-id"));
    }

    @Test
    void shouldFallbackToMetaInfoWhenContextIsNull() {
        assertEquals("meta-id", MessageIdMngUseCase.resolveForException("meta-id"));
    }

    @Test
    void shouldGenerateUuidWhenNoSource() {
        assertNotNull(MessageIdMngUseCase.resolveForException( null));
    }

    @Test
    void shouldGenerateUuidForExceptionWhenFlagIsNull() {
        new MessageIdMngUseCase(new MessageIdRequestProperties(null));
        assertNotNull(MessageIdMngUseCase.resolveForException( null));
    }


    @Test
    void shouldGenerateUuidWhenFlagIsNullAndNoHeader() {
        MessageIdMngUseCase useCaseNullFlag = new MessageIdMngUseCase(new MessageIdRequestProperties(null));
        assertNotNull(useCaseNullFlag.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldPropagateHeaderWhenFlagIsNullAndHeaderPresent() {
        MessageIdMngUseCase useCaseNullFlag = new MessageIdMngUseCase(new MessageIdRequestProperties(null));
        assertEquals("from-client", useCaseNullFlag.resolveFromRequestEnvironment("from-client"));
    }

    // ── resolveFromRequest con flag=false ─────────────────────────────────────

    @Test
    void shouldReturnNullWhenFlagIsFalseAndNoHeader() {
        // flag=false: no genera UUID, solo propaga si el cliente lo envía
        MessageIdMngUseCase useCaseFalse = new MessageIdMngUseCase(propsDisabled());
        assertNull(useCaseFalse.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldPropagateHeaderWhenFlagIsFalseAndHeaderPresent() {
        MessageIdMngUseCase useCaseFalse = new MessageIdMngUseCase(propsDisabled());
        assertEquals("from-client", useCaseFalse.resolveFromRequestEnvironment("from-client"));
    }
}
