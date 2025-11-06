package co.com.bancolombia.ecs.infra;

import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import co.com.bancolombia.ecs.model.request.LogRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class EcsReactiveLoggerTest {
    public static final String TEST_SERVICE = "testService";

    private static LogRequest getLogRequest() {
        LogRequest request = new LogRequest();
        request.setMessageId("message-id");
        request.setConsumer("consumer");
        request.setMethod("POST");
        request.setUrl("/api/v1/log");
        request.setHeaders(Map.of("key", "value"));
        request.setRequestBody(Map.of("key", "value"));
        request.setRequestBody(Map.of("key", "value"));
        request.setResponseResult("OK");
        request.setResponseCode("200");
        return request;
    }

    @Test
    void testBuildMiddleware() {
        AbstractMiddlewareEcsLog middleware = EcsReactiveLogger.build();
        assertNotNull(middleware);
    }

    @Test
    void testEcsPrivateConstructor() throws Exception {
        Constructor<EcsReactiveLogger> constructor = EcsReactiveLogger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        var state = constructor.newInstance();

        Assertions.assertNotNull(state);
    }

    @Test
    void testBuildWithThrowable() {
        Throwable mockThrowable = mock(Throwable.class);

        Throwable result = EcsReactiveLogger.build(mockThrowable, TEST_SERVICE).block();

        assertNotNull(result);
    }

    @Test
    void testBuildWithBusinessException() {
        var mockThrowable = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);

        Throwable result = EcsReactiveLogger.build(mockThrowable, TEST_SERVICE).block();

        assertNotNull(result);
    }

    @Test
    void testBuildWithException() {
        Throwable mockThrowable = mock(Exception.class);

        Throwable result = EcsReactiveLogger.build(mockThrowable, TEST_SERVICE).block();

        assertNotNull(result);
    }

    @Test
    void testBuildRequest() {
        LogRequest request = getLogRequest();
        var response = EcsReactiveLogger.build(request, TEST_SERVICE);
        StepVerifier.create(response)
            .verifyComplete();
    }

    @Test
    void testBuildRequestBusinessError() {
        LogRequest request = getLogRequest();
        var mockThrowable = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);
        request.setError(mockThrowable);
        var response = EcsReactiveLogger.build(request, TEST_SERVICE);
        StepVerifier.create(response)
            .verifyComplete();
    }

    @Test
    void testBuildRequestOtherError() {
        LogRequest request = getLogRequest();
        var mockThrowable = new RuntimeException("error");
        request.setError(mockThrowable);
        var response = EcsReactiveLogger.build(request, TEST_SERVICE);
        StepVerifier.create(response)
            .verifyComplete();
    }

}
