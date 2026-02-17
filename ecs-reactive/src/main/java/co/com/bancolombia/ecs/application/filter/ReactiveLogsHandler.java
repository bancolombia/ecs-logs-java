package co.com.bancolombia.ecs.application.filter;

import co.com.bancolombia.ecs.helpers.DataSanitizer;
import co.com.bancolombia.ecs.infra.EcsReactiveLogger;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.RequestLoggingDecorator;
import co.com.bancolombia.ecs.infra.config.ResponseLoggingDecorator;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.request.LogRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReactiveLogsHandler implements WebFilter {
    public static final String RAW_BODY = "raw";
    public static final Set<String> CONSUMER_ACRONYMS = Set.of("consumer-acronym", "code", "channel");
    public static final String MESSAGE_ID = "message-id";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final EcsPropertiesConfig ecsPropertiesConfig;
    private final Boolean showRequestLogs;
    private final Boolean showResponseLogs;

    public ReactiveLogsHandler(EcsPropertiesConfig ecsPropertiesConfig) {
        this.ecsPropertiesConfig = ecsPropertiesConfig;
        this.showRequestLogs = ecsPropertiesConfig.getShowRequestLogs();
        this.showResponseLogs = ecsPropertiesConfig.getShowResponseLogs();
    }

    private static void setConsumer(LogRequest requestInfo, Map<String, String> headers) {
        var consumer = CONSUMER_ACRONYMS.stream()
            .map(headers::get)
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
        requestInfo.setConsumer(consumer);
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (Boolean.FALSE.equals(showRequestLogs) && Boolean.FALSE.equals(showResponseLogs)) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (showRequestLogs && ecsPropertiesConfig.getExcludedPaths().stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

        var decoratedRequest = new RequestLoggingDecorator(
            exchange.getRequest(), bufferFactory);

        var decoratedResponse = new ResponseLoggingDecorator(
            exchange.getResponse(), bufferFactory
        );

        ServerWebExchange mutatedExchange = exchange
            .mutate()
            .request(decoratedRequest)
            .response(decoratedResponse)
            .build();

        return chain.filter(mutatedExchange)
            .then(Mono.defer(() -> logRequest(decoratedRequest, decoratedResponse)
                .subscribeOn(Schedulers.boundedElastic())))
            .onErrorResume(error -> logError(error, decoratedRequest)
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.error(error)));
    }

    private Mono<Void> logRequest(RequestLoggingDecorator request, ResponseLoggingDecorator response) {
        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);

        Mono<Void> requestBodyMono = getRequestBodyMono(request, logRequest);

        Mono<Void> responseBodyMono = getResponseBodyMono(response, logRequest);

        return Mono.when(requestBodyMono, responseBodyMono)
            .then(Mono.defer(() -> EcsReactiveLogger.build(logRequest, ecsPropertiesConfig.getServiceName()))
                .subscribeOn(Schedulers.boundedElastic()));
    }

    private Mono<Void> getResponseBodyMono(ResponseLoggingDecorator response, LogRequest logRequest) {
        return Boolean.TRUE.equals(showResponseLogs)
            ? readBody(response).flatMap(body -> Mono.fromCallable(() -> {
            HttpStatus status = (HttpStatus) response.getStatusCode();
            if (status != null) {
                logRequest.setResponseCode(String.valueOf(status.value()));
                logRequest.setResponseResult(status.getReasonPhrase());
            }
            String sanitized = DataSanitizer.sanitize(
                body, ecsPropertiesConfig.getSensitiveResponseFields(),
                ecsPropertiesConfig.getSensitiveResponsePatterns(),
                ecsPropertiesConfig.getSensitiveResponseReplacement());
            logRequest.setResponseBody(parseToMap(sanitized));
            return null;
        }).subscribeOn(Schedulers.boundedElastic())).then()
            : Mono.empty();
    }

    private Mono<Void> getRequestBodyMono(RequestLoggingDecorator request, LogRequest logRequest) {
        return Boolean.TRUE.equals(showRequestLogs)
            ? readBody(request).flatMap(body -> Mono.fromCallable(() -> {
            String sanitized = DataSanitizer.sanitize(
                body, ecsPropertiesConfig.getSensitiveRequestFields(),
                ecsPropertiesConfig.getSensitiveRequestPatterns(),
                ecsPropertiesConfig.getSensitiveRequestReplacement());
            logRequest.setRequestBody(parseToMap(sanitized));
            return null;
        }).subscribeOn(Schedulers.boundedElastic())).then()
            : Mono.empty();
    }

    private Mono<Void> logError(Throwable error, RequestLoggingDecorator request) {
        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);

        var status = resolveHttpStatus(error);
        logRequest.setResponseCode(String.valueOf(status.value()));
        logRequest.setResponseResult(status.getReasonPhrase());
        logRequest.setError(error);

        return readBody(request)
            .flatMap(body -> Mono.fromCallable(() -> {
                String sanitized = DataSanitizer.sanitize(
                    body, ecsPropertiesConfig.getSensitiveRequestFields(),
                    ecsPropertiesConfig.getSensitiveRequestPatterns(),
                    ecsPropertiesConfig.getSensitiveRequestReplacement());
                logRequest.setRequestBody(parseToMap(sanitized));
                return null;
            }).subscribeOn(Schedulers.boundedElastic()))
            .then(Mono.defer(() -> EcsReactiveLogger.build(logRequest, ecsPropertiesConfig.getServiceName()))
                .subscribeOn(Schedulers.boundedElastic()));
    }

    private Mono<String> readBody(RequestLoggingDecorator request) {
        return Mono.justOrEmpty(request.getBodyAsString());
    }

    private Mono<String> readBody(ResponseLoggingDecorator response) {
        return Mono.justOrEmpty(response.getBodyAsString());
    }

    private void setRequestParameters(RequestLoggingDecorator decoratedRequest, LogRequest requestInfo) {
        requestInfo.setMethod(decoratedRequest.getMethod().name());
        requestInfo.setUrl(decoratedRequest.getURI().getPath());

        Set<Map.Entry<String, List<String>>> headers =
                decoratedRequest.getHeaders()
                        .toSingleValueMap()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> List.of(e.getValue())
                        ))
                        .entrySet();

        Map<String, String> sanitizeHeaders = DataSanitizer.sanitizeHeaders(headers,
                ecsPropertiesConfig.getAllowRequestHeaders());
        setConsumer(requestInfo, sanitizeHeaders);
        requestInfo.setMessageId(sanitizeHeaders.get(MESSAGE_ID));
        requestInfo.setHeaders(sanitizeHeaders);
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
