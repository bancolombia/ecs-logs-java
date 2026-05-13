package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.infra.config.PrintOnErrorProperties;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.request.LogRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class HandlerHelper {
    public static final String ROOT_PATH = "/";
    public static final String RAW_BODY = "raw";
    public static final int MIN_ERROR_STATUS_CODE = 400;
    public static final Set<String> CONSUMER_ACRONYMS = Set.of("consumer-acronym", "code", "channel");
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setConsumer(LogRequest logRequest, Map<String, String> headers) {
        String consumer = CONSUMER_ACRONYMS.stream()
                .map(headers::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        logRequest.setConsumer(consumer);
    }

    public static HttpStatus resolveHttpStatus(Throwable ex) {
        if (ex instanceof BusinessExceptionECS businessExceptionECS) {
            HttpStatus resolvedStatus = resolveStatusCode(
                    businessExceptionECS.getConstantBusinessException() != null
                            ? businessExceptionECS.getConstantBusinessException().getStatus()
                            : null
            );
            if (resolvedStatus != null) {
                return resolvedStatus;
            }
        }
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus resolvedStatus = resolveStatusCode(errorResponse.getStatusCode().value());
            if (resolvedStatus != null) {
                return resolvedStatus;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static HttpStatus resolveStatusCode(Integer statusCode) {
        return statusCode != null
                ? HttpStatus.resolve(statusCode)
                : null;
    }

    public static Map<String, String> parseToMap(String body) {
        try {
            return objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of(RAW_BODY, body);
        }
    }

    public static boolean isErrorStatusCode(int statusCode) {
        return statusCode >= MIN_ERROR_STATUS_CODE;
    }

    public boolean isPathExcluded(String path, Set<String> excludedPaths) {
        return ROOT_PATH.equals(path)
                || (excludedPaths != null && excludedPaths.stream().anyMatch(path::startsWith));
    }

    public boolean errorDoesntMatchLevel(Throwable error, ExceptionLevel exceptionLevel) {
        return !PrintOnErrorProperties.matchesConfiguredLevel(error, exceptionLevel);
    }

    public static ResponseStatusException buildStatusException(HttpStatus status) {
        return new ResponseStatusException(status, status.getReasonPhrase());
    }
}
