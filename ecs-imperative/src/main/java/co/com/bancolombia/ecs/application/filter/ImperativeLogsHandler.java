package co.com.bancolombia.ecs.application.filter;

import co.com.bancolombia.ecs.helpers.DataSanitizer;
import co.com.bancolombia.ecs.infra.EcsImperativeLogger;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.request.LogRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImperativeLogsHandler extends OncePerRequestFilter {
    private static final Set<String> CONSUMER_ACRONYMS = Set.of("consumer-acronym", "code", "channel");
    private static final String HANDLED_EXCEPTION_PROPERTY = "handledException";
    private static final int MIN_REQUEST_ERROR_CODE = 400;
    private static final String MESSAGE_ID = "message-id";
    private static final String RAW_BODY = "raw";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final EcsPropertiesConfig ecsPropertiesConfig;
    private final Boolean showRequestLogs;
    private final Boolean showResponseLogs;

    public ImperativeLogsHandler(EcsPropertiesConfig ecsPropertiesConfig) {
        this.ecsPropertiesConfig = ecsPropertiesConfig;
        this.showRequestLogs = ecsPropertiesConfig.getShowRequestLogs();
        this.showResponseLogs = ecsPropertiesConfig.getShowResponseLogs();
    }

    private static void setConsumer(LogRequest logRequest, Map<String, String> headers) {
        String consumer = CONSUMER_ACRONYMS.stream()
            .map(headers::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        logRequest.setConsumer(consumer);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        if (Boolean.FALSE.equals(showRequestLogs) && Boolean.FALSE.equals(showResponseLogs)) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (ecsPropertiesConfig.getExcludedPaths().stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            if (status >= MIN_REQUEST_ERROR_CODE) {
                Throwable ex = (Throwable) wrappedRequest.getAttribute(HANDLED_EXCEPTION_PROPERTY);
                if (ex != null) {
                    logError(ex, wrappedRequest);
                } else {
                    logRequest(wrappedRequest, wrappedResponse);
                }
            } else {
                logRequest(wrappedRequest, wrappedResponse);
            }
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);

        if (Boolean.TRUE.equals(showRequestLogs)) {
            sensitiveRequestBody(request, logRequest);
        }

        if (Boolean.TRUE.equals(showResponseLogs)) {
            sensitiveResponseBody(response, logRequest);
        }

        EcsImperativeLogger.build(logRequest, ecsPropertiesConfig.getServiceName());
    }

    private void logError(Throwable error, ContentCachingRequestWrapper request) {
        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);
        sensitiveRequestBody(request, logRequest);

        // Response
        int status = resolveHttpStatus(error).value();
        logRequest.setResponseCode(String.valueOf(status));
        logRequest.setResponseResult(resolveHttpStatus(error).getReasonPhrase());
        logRequest.setError(error);

        EcsImperativeLogger.build(logRequest, ecsPropertiesConfig.getServiceName());
    }

    private void sensitiveResponseBody(ContentCachingResponseWrapper response, LogRequest logRequest) {
        int status = response.getStatus();
        logRequest.setResponseCode(String.valueOf(status));
        logRequest.setResponseResult(HttpStatus.valueOf(status).getReasonPhrase());

        var body = new String(response.getContentAsByteArray());
        String sanitizedResponse = DataSanitizer.sanitize(
            body,
            ecsPropertiesConfig.getSensitiveResponseFields(),
            ecsPropertiesConfig.getSensitiveResponsePatterns(),
            ecsPropertiesConfig.getSensitiveResponseReplacement());
        logRequest.setResponseBody(parseToMap(sanitizedResponse));
    }

    private void sensitiveRequestBody(ContentCachingRequestWrapper request, LogRequest logRequest) {
        var body = new String(request.getContentAsByteArray());
        String sanitizedRequest = DataSanitizer.sanitize(
            body,
            ecsPropertiesConfig.getSensitiveRequestFields(),
            ecsPropertiesConfig.getSensitiveRequestPatterns(),
            ecsPropertiesConfig.getSensitiveRequestReplacement());
        logRequest.setRequestBody(parseToMap(sanitizedRequest));
    }

    private void setRequestParameters(ContentCachingRequestWrapper request, LogRequest logRequest) {
        logRequest.setMethod(request.getMethod());
        logRequest.setUrl(request.getRequestURI());
        Set<Map.Entry<String, List<String>>> requestHeaders = Collections.list(request.getHeaderNames()).stream()
            .map(name -> Map.entry(name, List.of(request.getHeader(name).toLowerCase())))
            .collect(Collectors.toSet());

        Map<String, String> headers = DataSanitizer.sanitizeHeaders(requestHeaders,
            ecsPropertiesConfig.getAllowRequestHeaders());
        setConsumer(logRequest, headers);
        logRequest.setMessageId(headers.get(MESSAGE_ID));
        logRequest.setHeaders(headers);
    }

    private HttpStatus resolveHttpStatus(Throwable ex) {
        if (ex instanceof BusinessExceptionECS businessExceptionECS) {
            return HttpStatus.valueOf(businessExceptionECS.getConstantBusinessException().getStatus());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, String> parseToMap(String body) {
        try {
            return objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of(RAW_BODY, body);
        }
    }
}