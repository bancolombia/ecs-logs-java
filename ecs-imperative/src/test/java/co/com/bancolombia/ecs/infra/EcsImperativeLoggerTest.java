package co.com.bancolombia.ecs.infra;

import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import co.com.bancolombia.ecs.model.request.LogRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class EcsImperativeLoggerTest {
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
        AbstractMiddlewareEcsLog middleware = EcsImperativeLogger.build();
        assertNotNull(middleware);
    }

    @Test
    void testEcsPrivateConstructor() throws Exception {
        Constructor<EcsImperativeLogger> constructor = EcsImperativeLogger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        var state = constructor.newInstance();

        Assertions.assertNotNull(state);
    }

    @Test
    void testBuildWithThrowable() {
        Throwable mockThrowable = mock(Throwable.class);

        Throwable result = EcsImperativeLogger.build(mockThrowable, TEST_SERVICE);

        assertNotNull(result);
    }

    @Test
    void testBuildWithBusinessException() {
        var mockThrowable = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);

        Throwable result = EcsImperativeLogger.build(mockThrowable, TEST_SERVICE);

        assertNotNull(result);
    }

    @Test
    void testBuildWithException() {
        Throwable mockThrowable = mock(Exception.class);

        Throwable result = EcsImperativeLogger.build(mockThrowable, TEST_SERVICE);

        assertNotNull(result);
    }

    @Test
    void testBuildRequest() {
        LogRequest request = getLogRequest();
        EcsImperativeLogger.build(request, TEST_SERVICE);
        assertEquals("consumer", request.getConsumer());
        assertNotNull(request.getMessageId());
    }

    @Test
    void testBuildRequestBusinessError() {
        LogRequest request = getLogRequest();
        var mockThrowable = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);
        request.setError(mockThrowable);
        EcsImperativeLogger.build(request, TEST_SERVICE);
        assertEquals("message-id", request.getMessageId());
        assertNotNull(request.getConsumer());
    }

    @Test
    void testBuildRequestOtherError() {
        LogRequest request = getLogRequest();
        var mockThrowable = new RuntimeException("error");
        request.setError(mockThrowable);
        EcsImperativeLogger.build(request, TEST_SERVICE);
        assertNotNull(request.getRequestBody());
        assertNotNull(request.getRequestBody());
    }

}
