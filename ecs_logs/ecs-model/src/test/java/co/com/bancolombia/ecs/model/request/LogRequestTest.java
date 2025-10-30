package co.com.bancolombia.ecs.model.request;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogRequestTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        String messageId = "123";
        String consumer = "test-consumer";
        String method = "GET";
        String url = "http://example.com";
        Throwable error = new RuntimeException("Error");
        Map<String, String> headers = Map.of("header1", "value1");
        Map<String, String> requestBody = Map.of("key", "value");
        Map<String, String> responseBody = Map.of("resp", "data");
        String responseResult = "SUCCESS";
        String responseCode = "200";

        LogRequest logRequest = new LogRequest(
            messageId, consumer, method, url, error,
            headers, requestBody, responseBody, responseResult, responseCode
        );

        assertEquals(messageId, logRequest.getMessageId());
        assertEquals(consumer, logRequest.getConsumer());
        assertEquals(method, logRequest.getMethod());
        assertEquals(url, logRequest.getUrl());
        assertEquals(error, logRequest.getError());
        assertEquals(headers, logRequest.getHeaders());
        assertEquals(requestBody, logRequest.getRequestBody());
        assertEquals(responseBody, logRequest.getResponseBody());
        assertEquals(responseResult, logRequest.getResponseResult());
        assertEquals(responseCode, logRequest.getResponseCode());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        LogRequest logRequest = new LogRequest();

        Throwable error = new IllegalArgumentException("Bad request");

        logRequest.setMessageId("abc");
        logRequest.setConsumer("service-A");
        logRequest.setMethod("POST");
        logRequest.setUrl("http://localhost");
        logRequest.setError(error);
        logRequest.setHeaders(Map.of("Authorization", "Bearer token"));
        logRequest.setRequestBody(Map.of("user", "admin"));
        logRequest.setResponseBody(Map.of("status", "ok"));
        logRequest.setResponseResult("OK");
        logRequest.setResponseCode("201");

        assertEquals("abc", logRequest.getMessageId());
        assertEquals("service-A", logRequest.getConsumer());
        assertEquals("POST", logRequest.getMethod());
        assertEquals("http://localhost", logRequest.getUrl());
        assertEquals(error, logRequest.getError());
        assertEquals("Bearer token", logRequest.getHeaders().get("Authorization"));
        assertEquals("admin", logRequest.getRequestBody().get("user"));
        assertEquals("ok", logRequest.getResponseBody().get("status"));
        assertEquals("OK", logRequest.getResponseResult());
        assertEquals("201", logRequest.getResponseCode());
    }

}
