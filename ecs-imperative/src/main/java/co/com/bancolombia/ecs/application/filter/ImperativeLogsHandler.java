package co.com.bancolombia.ecs.application.filter;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.helpers.DataSanitizer;
import co.com.bancolombia.ecs.helpers.HandlerHelper;
import co.com.bancolombia.ecs.infra.EcsImperativeLogger;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.model.request.LogRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImperativeLogsHandler extends OncePerRequestFilter {
    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;
    private static final String HANDLED_EXCEPTION_PROPERTY = "handledException";
    private final EcsPropertiesConfig ecsPropertiesConfig;
    private final Boolean showRequestLogs;
    private final Boolean showResponseLogs;
    private final boolean printReqRespOnErrorOnlyActive;
    private final ExceptionLevel printReqRespLevel;
    private final MessageIdMngUseCase messageIdMngUseCase;

    public ImperativeLogsHandler(EcsPropertiesConfig ecsPropertiesConfig,
                                 MessageIdMngUseCase messageIdMngUseCase) {
        this.ecsPropertiesConfig = ecsPropertiesConfig;
        this.showRequestLogs = ecsPropertiesConfig.getShowRequestLogs();
        this.showResponseLogs = ecsPropertiesConfig.getShowResponseLogs();
        this.printReqRespOnErrorOnlyActive = Boolean.TRUE.equals(ecsPropertiesConfig.getPrintReqRespOnErrorOnly());
        this.printReqRespLevel = ecsPropertiesConfig.getPrintReqRespLevels();
        this.messageIdMngUseCase = messageIdMngUseCase;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String resolvedMessageId = resolveMessageId(request);
        if (resolvedMessageId != null) {
            ContextECS.setMessageId(resolvedMessageId);
        }

        if (!printReqRespOnErrorOnlyActive
                && Boolean.FALSE.equals(showRequestLogs)
                && Boolean.FALSE.equals(showResponseLogs)) {
            doFilterWithClear(chain, request, response);
            return;
        }

        String path = request.getRequestURI();
        if (HandlerHelper.isPathExcluded(path, ecsPropertiesConfig.getExcludedPaths())) {
            doFilterWithClear(chain, request, response);
            return;
        }

        var wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_SIZE);
        var wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            handleRequestOrError(wrappedRequest, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
            ContextECS.clear();
        }
    }

    private String resolveMessageId(HttpServletRequest request) {
        Map<String, List<String>> rawHeadersMap = Collections.list(request.getHeaderNames()).stream()
                .filter(name -> request.getHeader(name) != null)
                .collect(Collectors.toMap(name -> name, name -> List.of(request.getHeader(name))));
        return messageIdMngUseCase.resolveFromHeaders(
                rawHeadersMap.entrySet(), ecsPropertiesConfig.getAllowRequestHeaders());
    }

    private void doFilterWithClear(FilterChain chain, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            ContextECS.clear();
        }
    }

    private void handleRequestOrError(ContentCachingRequestWrapper wrappedRequest,
                                      ContentCachingResponseWrapper wrappedResponse) {
        int status = wrappedResponse.getStatus();
        if (HandlerHelper.isErrorStatusCode(status)) {
            Throwable ex = (Throwable) wrappedRequest.getAttribute(HANDLED_EXCEPTION_PROPERTY);
            logError(ex != null ? ex : buildStatusException(status), wrappedRequest);
        } else {
            logRequest(wrappedRequest, wrappedResponse);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (printReqRespOnErrorOnlyActive) {
            return;
        }

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
        if (printReqRespOnErrorOnlyActive && HandlerHelper.errorDoesntMatchLevel(error, printReqRespLevel)) {
            return;
        }

        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);
        sensitiveRequestBody(request, logRequest);

        // Response
        var status = HandlerHelper.resolveHttpStatus(error);
        logRequest.setResponseCode(String.valueOf(status.value()));
        logRequest.setResponseResult(status.getReasonPhrase());
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
        logRequest.setResponseBody(HandlerHelper.parseToMap(sanitizedResponse));
    }

    private void sensitiveRequestBody(ContentCachingRequestWrapper request, LogRequest logRequest) {
        var body = new String(request.getContentAsByteArray());
        String sanitizedRequest = DataSanitizer.sanitize(
            body,
            ecsPropertiesConfig.getSensitiveRequestFields(),
            ecsPropertiesConfig.getSensitiveRequestPatterns(),
            ecsPropertiesConfig.getSensitiveRequestReplacement());
        logRequest.setRequestBody(HandlerHelper.parseToMap(sanitizedRequest));
    }

    private void setRequestParameters(ContentCachingRequestWrapper request, LogRequest logRequest) {
        logRequest.setMethod(request.getMethod());
        logRequest.setUrl(request.getRequestURI());
        Set<Map.Entry<String, List<String>>> requestHeaders = Collections.list(request.getHeaderNames()).stream()
            .flatMap(name -> {
                String headerValue = request.getHeader(name);
                return headerValue == null
                    ? Stream.empty()
                    : Stream.of(Map.entry(name, List.of(headerValue.toLowerCase())));
            })
            .collect(Collectors.toSet());

        Map<String, String> headers = DataSanitizer.sanitizeHeaders(requestHeaders,
            ecsPropertiesConfig.getAllowRequestHeaders());
        HandlerHelper.setConsumer(logRequest, headers);
        logRequest.setMessageId(headers.get(LogRecord.MESSAGE_ID));
        logRequest.setHeaders(headers);
    }

    private ResponseStatusException buildStatusException(int statusCode) {
        HttpStatus status = HandlerHelper.resolveStatusCode(statusCode);
        HttpStatus resolvedStatus = status != null ? status:HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseStatusException(resolvedStatus, resolvedStatus.getReasonPhrase());
    }
}
