package co.com.bancolombia.api.shared.common.application;

import co.com.bancolombia.api.shared.common.domain.response.SuccessApiResponse;
import co.com.bancolombia.model.shared.common.value.Constants;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@UtilityClass
public class HandleResponse {
    public static Mono<ServerResponse> createSuccessResponse(Object content, ContextData contextData,
                                                             HttpStatus status, ServerRequest serverRequest) {
        return ServerResponse
                .status(status)
                .headers(buildHeaders(serverRequest))
                .bodyValue(SuccessApiResponse.build(content, contextData));
    }

    private Consumer<HttpHeaders> buildHeaders(ServerRequest serverRequest) {
        return headers -> {
            HttpHeaders filteredHeaders = new HttpHeaders();
            serverRequest.headers().asHttpHeaders().forEach((key, values) -> {
                if (!isProxyHeader(key.toLowerCase())) {
                    filteredHeaders.addAll(key, values);
                }
            });
            headers.addAll(filteredHeaders);
        };
    }

    private boolean isProxyHeader(String headerName) {
        return Constants.PROXY_HEADERS.contains(headerName);
    }
}
