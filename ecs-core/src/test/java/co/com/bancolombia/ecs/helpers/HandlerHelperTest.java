package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import co.com.bancolombia.ecs.model.request.LogRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandlerHelperTest {

    @Test
    void testSetConsumerShouldUseConsumerAcronymHeader() {
        LogRequest logRequest = new LogRequest();

        HandlerHelper.setConsumer(logRequest, Map.of("consumer-acronym", "mobile-app"));

        assertEquals("mobile-app", logRequest.getConsumer());
    }

    @Test
    void testSetConsumerShouldLeaveConsumerNullWhenHeadersDoNotContainKnownKeys() {
        LogRequest logRequest = new LogRequest();

        HandlerHelper.setConsumer(logRequest, Map.of("message-id", "12345"));

        assertNull(logRequest.getConsumer());
    }

    @Test
    void testResolveHttpStatusShouldPreserveResponseStatusExceptionStatus() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid CORS request");

        HttpStatus resolvedStatus = HandlerHelper.resolveHttpStatus(exception);

        assertEquals(HttpStatus.FORBIDDEN, resolvedStatus);
    }

    @Test
    void testResolveHttpStatusShouldReturnBusinessExceptionStatus() {
        BusinessExceptionECS exception = new BusinessExceptionECS(errorManagementWithStatus(HttpStatus.BAD_REQUEST.value()));

        HttpStatus resolvedStatus = HandlerHelper.resolveHttpStatus(exception);

        assertEquals(HttpStatus.BAD_REQUEST, resolvedStatus);
    }

    @Test
    void testResolveHttpStatusShouldFallbackToInternalServerErrorWhenBusinessStatusIsNull() {
        BusinessExceptionECS exception = new BusinessExceptionECS(errorManagementWithStatus(null));

        HttpStatus resolvedStatus = HandlerHelper.resolveHttpStatus(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resolvedStatus);
    }

    @Test
    void testResolveStatusCodeShouldResolveKnownValuesAndNulls() {
        assertEquals(HttpStatus.OK, HandlerHelper.resolveStatusCode(HttpStatus.OK.value()));
        assertNull(HandlerHelper.resolveStatusCode(999));
    }

    @Test
    void testParseToMapShouldReturnParsedJsonMap() {
        Map<String, String> body = HandlerHelper.parseToMap("{\"key\":\"value\"}");

        assertEquals(Map.of("key", "value"), body);
    }

    @Test
    void testParseToMapShouldWrapRawBodyWhenJsonIsInvalid() {
        String invalidBody = "plain-text-body";

        Map<String, String> body = HandlerHelper.parseToMap(invalidBody);

        assertEquals(Map.of(HandlerHelper.RAW_BODY, invalidBody), body);
    }

    @Test
    void testIsErrorStatusCodeShouldOnlyReturnTrueFor4xxAnd5xxStatuses() {
        assertFalse(HandlerHelper.isErrorStatusCode(HttpStatus.OK.value()));
        assertTrue(HandlerHelper.isErrorStatusCode(HttpStatus.BAD_REQUEST.value()));
        assertTrue(HandlerHelper.isErrorStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    private ErrorManagement errorManagementWithStatus(Integer status) {
        return new ErrorManagement() {
            @Override
            public Integer getStatus() {
                return status;
            }

            @Override
            public String getMessage() {
                return "Invalid CORS request";
            }

            @Override
            public String getErrorCode() {
                return "CORS-403";
            }

            @Override
            public String getInternalMessage() {
                return "CORS validation failed";
            }

            @Override
            public String getLogCode() {
                return "CORS-403-00";
            }
        };
    }
}



