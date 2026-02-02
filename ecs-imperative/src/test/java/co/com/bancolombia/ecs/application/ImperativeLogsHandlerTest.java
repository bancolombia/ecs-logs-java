package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ImperativeLogsHandler;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImperativeLogsHandlerTest {

    @Mock
    private ServiceProperties serviceProperties;

    @Mock
    private SensitiveRequestProperties sensitiveRequestProperties;

    @Mock
    private SensitiveResponseProperties sensitiveResponseProperties;

    @Mock
    private EcsPropertiesConfig ecsPropertiesConfig;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ImperativeLogsHandler imperativeLogsHandler;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private ContentCachingRequestWrapper wrappedRequest;
    private ContentCachingResponseWrapper wrappedResponse;

    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        wrappedRequest = new ContentCachingRequestWrapper(mockRequest, MAX_PAYLOAD_SIZE);
        wrappedResponse = new ContentCachingResponseWrapper(mockResponse);
        imperativeLogsHandler = new ImperativeLogsHandler(ecsPropertiesConfig);
    }

    @Test
    void testShouldSkipFilterForExcludedPath() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/actuator/health/check");

        imperativeLogsHandler.doFilter(mockRequest, mockResponse, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldSkipFilterWhenLogsAreDisabled() throws IOException, ServletException {
        mocksPropertiesConfig(false, false);
        mockRequest.setRequestURI("/api/test");

        imperativeLogsHandler.doFilter(mockRequest, mockResponse, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldProcessRequestAndResponseLogs() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        testFilter();
    }

    @Test
    void testShouldProcessRequestLogs() throws IOException, ServletException {
        mocksPropertiesConfig(true, false);
        testFilter();
    }

    @Test
    void testShouldProcessResponseLogs() throws IOException, ServletException {
        mocksPropertiesConfig(false, true);
        testFilter();
    }

    @Test
    void testShouldHandleBusinessExceptionECS() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());
        BusinessExceptionECS exception = new BusinessExceptionECS("error");
        doThrow(exception).when(filterChain).doFilter(any(), any());

        assertThrows(BusinessExceptionECS.class, () ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldHandleGenericException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());
        RuntimeException exception = new RuntimeException("Test error");
        doThrow(exception).when(filterChain).doFilter(wrappedRequest, wrappedResponse);

        assertThrows(RuntimeException.class, () ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogRequestOnErrorStatusWithException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());

        wrappedResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");

        var exception = new BusinessExceptionECS("error");
        wrappedRequest.setAttribute("handledException", exception);

        assertDoesNotThrow(() ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogRequestOnErrorStatusWithoutException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());

        wrappedResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");

        assertDoesNotThrow(() ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogRequestOnErrorStatusWithUnhandledException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());

        wrappedResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");

        var exception = new RuntimeException("error");
        wrappedRequest.setAttribute("handledException", exception);

        assertDoesNotThrow(() ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }


    private void testFilter() throws IOException, ServletException {
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("POST");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.addHeader("consumer-acronym", "test-consumer");
        mockRequest.setContent("{\"data\": \"test\", \"password\": \"secret\"}".getBytes());
        mockResponse.setStatus(HttpStatus.OK.value());
        mockResponse.getWriter().write("{\"result\": \"success\", \"secret\": \"hidden\"}");
        wrappedResponse.copyBodyToResponse();
        wrappedResponse.setStatus(HttpStatus.OK.value());
        assertDoesNotThrow(() -> imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    private void mocksPropertiesConfig(boolean showRequest, boolean showResponse) {
        when(serviceProperties.getName()).thenReturn("test-service");
        when(sensitiveRequestProperties.getShow()).thenReturn(showRequest);

        if (showRequest) {
            when(sensitiveRequestProperties.getDelimiter()).thenReturn("\\|");
            when(sensitiveRequestProperties.getAllowHeaders()).thenReturn("message-id|code|channel|acronym-channel");
            when(sensitiveRequestProperties.getFields()).thenReturn("");
            when(sensitiveRequestProperties.getExcludedPaths()).thenReturn("/actuator");
            when(sensitiveRequestProperties.getPatterns()).thenReturn("");
            when(sensitiveRequestProperties.getReplacement()).thenReturn("*****");
        }

        when(sensitiveResponseProperties.getShow()).thenReturn(showResponse);

        if (showResponse) {
            when(sensitiveResponseProperties.getDelimiter()).thenReturn("\\|");
            when(sensitiveResponseProperties.getFields()).thenReturn("");
            when(sensitiveResponseProperties.getPatterns()).thenReturn("");
            when(sensitiveResponseProperties.getReplacement()).thenReturn("*****");
        }

        ecsPropertiesConfig = new EcsPropertiesConfig(serviceProperties, sensitiveRequestProperties,
            sensitiveResponseProperties);
        imperativeLogsHandler = new ImperativeLogsHandler(ecsPropertiesConfig);
    }
}