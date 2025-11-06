package co.com.bancolombia.api.shared.common.application;


import co.com.bancolombia.api.shared.common.domain.HeaderConstant;
import co.com.bancolombia.api.shared.common.domain.response.ErrorApiResponse;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.model.shared.common.value.Constants;
import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public static final String FAILED_SUFFIX = "-FALLIDO";

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ApplicationContext applicationContext,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::buildErrorResponse);
    }

    private Mono<ServerResponse> buildErrorResponse(ServerRequest request) {
        return accessError(request)
                .flatMap(Mono::error)
                .onErrorResume(BusinessException.class, exception -> createResponseFromBusiness(exception, request))
                .onErrorResume(throwable -> unknownError(throwable, request))
                .cast(ServerResponse.class);
    }

    public Mono<ServerResponse> createResponseFromBusiness(BusinessException exception, ServerRequest request) {
        return ServerResponse
                .status(exception.getConstantBusinessException().getStatus())
                .headers(buildHeaders(request))
                .bodyValue(ErrorApiResponse.build(exception));
    }

    private Consumer<HttpHeaders> buildHeaders(ServerRequest serverRequest) {
        return headers -> {
            HttpHeaders filteredHeaders = new HttpHeaders();
            serverRequest.headers().asHttpHeaders().forEach((key, values) -> {
                if (!isProxyHeader(key.toLowerCase())) {
                    filteredHeaders.addAll(key, values);
                }
            });

            headers.addAll(HeaderResponse.addAdditionalHeaders());
            if (headers.getFirst(HeaderConstant.X_REQUEST_ID.value()) == null
                    || Objects.requireNonNull(headers.getFirst(HeaderConstant.X_REQUEST_ID.value())).isBlank()) {
                var messageId = headers.getFirst(HeaderConstant.MESSAGE_ID.value());
                if (messageId != null) {
                    headers.set(HeaderConstant.X_REQUEST_ID.value(), messageId);
                } else {
                    headers.set(HeaderConstant.X_REQUEST_ID.value(), UUID.randomUUID().toString());
                }
            }
        };
    }

    private boolean isProxyHeader(String headerName) {
        return Constants.PROXY_HEADERS.contains(headerName);
    }

    public Mono<ServerResponse> unknownError(Throwable exception, ServerRequest request) {
        var metaInfo = BusinessExceptionECS.MetaInfo.builder()
                .messageId(request.headers().firstHeader(HeaderConstant.MESSAGE_ID.value()))
                .build();
        var businessException = new BusinessException(ConstantBusinessException.DEFAULT_EXCEPTION,
                exception.getMessage(), metaInfo);
        return createResponseFromBusiness(businessException, request);
    }

    private Mono<Throwable> accessError(ServerRequest request) {
        return Mono.just(request)
                .map(this::getError);
    }
}
