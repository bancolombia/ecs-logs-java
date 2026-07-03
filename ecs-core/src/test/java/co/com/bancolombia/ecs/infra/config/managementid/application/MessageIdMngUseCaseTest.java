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
    void shouldReturnNullWhenFlagIsNullAndNoHeader() {
        MessageIdMngUseCase useCaseNullFlag = new MessageIdMngUseCase(new MessageIdRequestProperties(null));
        assertNull(useCaseNullFlag.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldPropagateHeaderWhenFlagIsNullAndHeaderPresent() {
        MessageIdMngUseCase useCaseNullFlag = new MessageIdMngUseCase(new MessageIdRequestProperties(null));
        assertEquals("from-client", useCaseNullFlag.resolveFromRequestEnvironment("from-client"));
    }


    @Test
    void shouldReturnNullWhenFlagIsFalseAndNoHeader() {
        MessageIdMngUseCase useCaseFalse = new MessageIdMngUseCase(propsDisabled());
        assertNull(useCaseFalse.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldPropagateHeaderWhenFlagIsFalseAndHeaderPresent() {
        MessageIdMngUseCase useCaseFalse = new MessageIdMngUseCase(propsDisabled());
        assertEquals("from-client", useCaseFalse.resolveFromRequestEnvironment("from-client"));
    }

    @Test
    void shouldNotThrowWhenValueIsBlank() {
        MessageIdRequestProperties props = new MessageIdRequestProperties("   ");
        assertDoesNotThrow(props::afterPropertiesSet);
        assertNull(props.getEnabled());
    }

    @Test
    void shouldReturnNullWhenFlagIsBlankAndNoHeader() {
        MessageIdRequestProperties props = new MessageIdRequestProperties("");
        props.afterPropertiesSet();
        MessageIdMngUseCase useCaseBlankFlag = new MessageIdMngUseCase(props);
        assertNull(useCaseBlankFlag.resolveFromRequestEnvironment(null));
    }

    @Test
    void shouldPropagateHeaderWhenFlagIsBlankAndHeaderPresent() {
        MessageIdRequestProperties props = new MessageIdRequestProperties("");
        props.afterPropertiesSet();
        MessageIdMngUseCase useCaseBlankFlag = new MessageIdMngUseCase(props);
        assertEquals("from-client", useCaseBlankFlag.resolveFromRequestEnvironment("from-client"));
    }
}
