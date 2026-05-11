package co.com.bancolombia.ecs.domain.middleware;

import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class MiddlewareEcsBusinessTest {

    private MiddlewareEcsBusiness middleware;

    @BeforeEach
    void setUp() {
        middleware = new MiddlewareEcsBusiness();
        ContextECS.clear();
    }

    @AfterEach
    void tearDown() {
        ContextECS.clear();
    }

    @Test
    void shouldUseContextMessageIdWhenAvailable(CapturedOutput output) {
        ContextECS.setMessageId("ctx-message-id-123");
        var exception = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);

        middleware.process(exception, "test-service");

        assertTrue(output.getOut().contains("ctx-message-id-123"));
    }

    @Test
    void shouldUseMetaInfoMessageIdWhenContextIsAbsent(CapturedOutput output) {
        String expectedMessageId = "business-exception-message-id";
        var metaInfo = BusinessExceptionECS.MetaInfo.builder()
                .messageId(expectedMessageId)
                .build();
        var exception = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION, metaInfo);

        middleware.process(exception, "test-service");

        assertTrue(output.getOut().contains(expectedMessageId));
    }

    @Test
    void shouldGenerateUuidWhenNoMessageIdFromAnySource(CapturedOutput output) {
        var metaInfo = BusinessExceptionECS.MetaInfo.builder()
                .messageId(null)
                .build();
        var exception = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION, metaInfo);

        middleware.process(exception, "test-service");

        String log = output.getOut();
        assertTrue(log.contains("message-id"));
        assertFalse(log.contains("\"message-id\":null"));
    }

    @Test
    void shouldDelegateToNextWhenRequestIsNotBusinessException(CapturedOutput output) {
        assertDoesNotThrow(() -> new MiddlewareEcsBusiness().process("not-an-exception", "test-service"));
        assertTrue(output.getOut().isEmpty());
    }
}
