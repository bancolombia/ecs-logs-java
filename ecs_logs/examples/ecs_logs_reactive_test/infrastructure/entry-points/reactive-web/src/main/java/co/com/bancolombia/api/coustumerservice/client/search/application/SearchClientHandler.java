package co.com.bancolombia.api.coustumerservice.client.search.application;

import co.com.bancolombia.api.coustumerservice.client.search.application.validation.HeadersValidatorSearchParametersClient;
import co.com.bancolombia.api.coustumerservice.client.search.domain.SearchClientResponse;
import co.com.bancolombia.api.coustumerservice.client.search.infra.TransformRequest;
import co.com.bancolombia.api.shared.common.application.HandleResponse;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClient;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClientIdentification;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.cqrs.Query;
import co.com.bancolombia.usecase.coustumerservice.client.search.SearchClientUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SearchClientHandler {
    private final SearchClientUseCase searchClientParametersUseCase;

    public Mono<ServerResponse> searchClient(ServerRequest request) {
        return HeadersValidatorSearchParametersClient.validateHeaders(request)
                .flatMap(contextData -> TransformRequest.fromRequest(request, contextData)
                        .flatMap(searchClientIdentification -> callUseCase(searchClientIdentification, contextData))
                        .flatMap(searchClient -> buildResponse(searchClient, contextData, request)));

    }

    private Mono<SearchClient> callUseCase(SearchClientIdentification searchClientIdentification,
                                           ContextData contextData) {
        var query = new Query<>(searchClientIdentification, contextData);
        return searchClientParametersUseCase.search(query);
    }

    private Mono<ServerResponse> buildResponse(SearchClient searchClient, ContextData contextData,
                                               ServerRequest serverRequest) {
        var dataResponse = SearchClientResponse.builder()
                .clientName(searchClient.getClientName())
                .identification(searchClient.getIdentification())
                .address(searchClient.getAddress())
                .city(searchClient.getCity())
                .build();
        return HandleResponse.createSuccessResponse(dataResponse, contextData,
                HttpStatus.OK, serverRequest);
    }
}
