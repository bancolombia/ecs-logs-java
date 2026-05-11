package co.com.bancolombia.ecs.application.filter;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.helpers.DataSanitizer;
import co.com.bancolombia.ecs.helpers.HandlerHelper;
import co.com.bancolombia.ecs.infra.EcsReactiveLogger;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.RequestLoggingDecorator;
import co.com.bancolombia.ecs.infra.config.ResponseLoggingDecorator;
import co.com.bancolombia.ecs.model.request.LogRequest;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;
import reactor.util.context.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Order(Integer.MIN_VALUE)
public class ReactiveLogsHandler implements WebFilter {
    private final EcsPropertiesConfig ecsPropertiesConfig;
    private final Boolean showRequestLogs;
    private final Boolean showResponseLogs;
    private final boolean printReqRespOnErrorOnlyActive;
    private final ExceptionLevel printReqRespLevel;
    private final MessageIdMngUseCase messageIdMngUseCase;

    public ReactiveLogsHandler(EcsPropertiesConfig ecsPropertiesConfig,
                               MessageIdMngUseCase messageIdMngUseCase) {
        this.ecsPropertiesConfig = ecsPropertiesConfig;
        this.showRequestLogs = ecsPropertiesConfig.getShowRequestLogs();
        this.showResponseLogs = ecsPropertiesConfig.getShowResponseLogs();
        this.printReqRespOnErrorOnlyActive = Boolean.TRUE.equals(ecsPropertiesConfig.getPrintReqRespOnErrorOnly());
        this.printReqRespLevel = ecsPropertiesConfig.getPrintReqRespLevels();
        this.messageIdMngUseCase = messageIdMngUseCase;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String messageId = messageIdMngUseCase.resolveFromHeaders(
                extractRequestHeaders(exchange), ecsPropertiesConfig.getAllowRequestHeaders());

        if (messageId != null) {
            exchange.getAttributes().put(ContextECS.KEY_MESSAGE_ID, messageId);
        }

        return buildFilterChain(exchange, chain)
                .contextWrite(ctx -> writeMessageIdToContext(ctx, messageId));
    }


    private Mono<Void> buildFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        if (!printReqRespOnErrorOnlyActive
                && Boolean.FALSE.equals(showRequestLogs)
                && Boolean.FALSE.equals(showResponseLogs)) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (HandlerHelper.isPathExcluded(path, ecsPropertiesConfig.getExcludedPaths())) {
            return chain.filter(exchange);
        }

        var bufferFactory = new DefaultDataBufferFactory();

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
                .then(Mono.defer(() -> logRequestOrError(decoratedRequest, decoratedResponse)
                        .subscribeOn(Schedulers.boundedElastic())))
                .onErrorResume(error -> logError(error, decoratedRequest)
                        .subscribeOn(Schedulers.boundedElastic())
                        .then(Mono.error(error)));
    }

    private Set<Map.Entry<String, List<String>>> extractRequestHeaders(ServerWebExchange exchange) {
        Set<Map.Entry<String, List<String>>> headers = new HashSet<>();
        exchange.getRequest().getHeaders().forEach((key, values) -> headers.add(Map.entry(key, values)));
        return headers;
    }

    private Context writeMessageIdToContext(Context ctx, String messageId) {
        return messageId != null ? ctx.put(ContextECS.KEY_MESSAGE_ID, messageId) : ctx;
    }

    private void applyContextMessageId(LogRequest logRequest) {
        if (logRequest.getMessageId() == null) {
            MessageIdMngUseCase.getFromContext().ifPresent(logRequest::setMessageId);
        }
    }

    private Mono<Void> logRequestOrError(RequestLoggingDecorator request, ResponseLoggingDecorator response) {
        var status = resolveHttpStatus(response);
        if (status != null && HandlerHelper.isErrorStatusCode(status.value())) {
            return logError(HandlerHelper.buildStatusException(status), request);
        }
        return logRequest(request, response);
    }

    private Mono<Void> logRequest(RequestLoggingDecorator request, ResponseLoggingDecorator response) {
        if (printReqRespOnErrorOnlyActive) {
            return Mono.empty();
        }

        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);
        applyContextMessageId(logRequest);

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
            logRequest.setResponseBody(HandlerHelper.parseToMap(sanitized));
            return null;
        }).subscribeOn(Schedulers.boundedElastic())).then()
            : Mono.empty();
    }

    private Mono<String> readBody(ResponseLoggingDecorator response) {
        return Mono.justOrEmpty(response.getBodyAsString());
    }

    private Mono<Void> getRequestBodyMono(RequestLoggingDecorator request, LogRequest logRequest) {
        return Boolean.TRUE.equals(showRequestLogs)
            ? readBody(request).flatMap(body -> Mono.fromCallable(() -> {
            String sanitized = DataSanitizer.sanitize(
                body, ecsPropertiesConfig.getSensitiveRequestFields(),
                ecsPropertiesConfig.getSensitiveRequestPatterns(),
                ecsPropertiesConfig.getSensitiveRequestReplacement());
            logRequest.setRequestBody(HandlerHelper.parseToMap(sanitized));
            return null;
        }).subscribeOn(Schedulers.boundedElastic())).then()
            : Mono.empty();
    }

    private Mono<Void> logError(Throwable error, RequestLoggingDecorator request) {
        if (printReqRespOnErrorOnlyActive && HandlerHelper.errorDoesntMatchLevel(error, printReqRespLevel)) {
            return Mono.empty();
        }

        var logRequest = new LogRequest();
        setRequestParameters(request, logRequest);
        applyContextMessageId(logRequest);

        var status = HandlerHelper.resolveHttpStatus(error);
        logRequest.setResponseCode(String.valueOf(status.value()));
        logRequest.setResponseResult(status.getReasonPhrase());
        logRequest.setError(error);

        return readBody(request)
                .flatMap(body -> Mono.fromCallable(() -> {
                    String sanitized = DataSanitizer.sanitize(
                            body, ecsPropertiesConfig.getSensitiveRequestFields(),
                            ecsPropertiesConfig.getSensitiveRequestPatterns(),
                            ecsPropertiesConfig.getSensitiveRequestReplacement());
                    logRequest.setRequestBody(HandlerHelper.parseToMap(sanitized));
                    return null;
                }).subscribeOn(Schedulers.boundedElastic()))
                .then(Mono.defer(() -> EcsReactiveLogger.build(logRequest, ecsPropertiesConfig.getServiceName()))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private Mono<String> readBody(RequestLoggingDecorator request) {
        return Mono.justOrEmpty(request.getBodyAsString());
    }

    private void setRequestParameters(RequestLoggingDecorator decoratedRequest, LogRequest requestInfo) {
        requestInfo.setMethod(decoratedRequest.getMethod().name());
        requestInfo.setUrl(decoratedRequest.getURI().getPath());

        Set<Map.Entry<String, List<String>>> headers = new HashSet<>();
        decoratedRequest.getHeaders().forEach((key, values) -> headers.add(Map.entry(key, values)));

        Map<String, String> sanitizeHeaders = DataSanitizer.sanitizeHeaders(headers,
                ecsPropertiesConfig.getAllowRequestHeaders());
        HandlerHelper.setConsumer(requestInfo, sanitizeHeaders);
        requestInfo.setMessageId(sanitizeHeaders.get(LogRecord.MESSAGE_ID));
        requestInfo.setHeaders(sanitizeHeaders);
    }

    private HttpStatus resolveHttpStatus(ResponseLoggingDecorator response) {
        var statusCode = response.getStatusCode();
        return statusCode != null ? HandlerHelper.resolveStatusCode(statusCode.value()) : null;
    }
}
