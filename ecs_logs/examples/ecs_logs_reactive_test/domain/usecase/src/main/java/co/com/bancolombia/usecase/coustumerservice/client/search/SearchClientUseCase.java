package co.com.bancolombia.usecase.coustumerservice.client.search;

import co.com.bancolombia.model.coustumerservice.client.search.gateway.SearchClientGateway;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClient;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClientIdentification;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.cqrs.Query;
import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;
import co.com.bancolombia.model.shared.exception.SystemException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SearchClientUseCase {
    private final SearchClientGateway searchClient;

    public Mono<SearchClient> search(
            Query<SearchClientIdentification, ContextData> query) {
        return searchClientParameters(query);
    }

    private Mono<SearchClient> searchClientParameters(
            Query<SearchClientIdentification, ContextData> query) {
        var querySearch = new Query<>(query.payload().getIdentification().getDocumentNumber()
                .getValue(), query.context());
        return searchClient.getClientByIdentification(querySearch)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new BusinessException(
                                ConstantBusinessException.NOT_FOUND_REGISTER_RETRIEVE_PARAM_CLIENT_02,
                                query.context()))
                ))
                .filter(searchQuery -> !searchQuery.getIdentification().equals("8459312"))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new SystemException(
                                ConstantBusinessException.DEFAULT_EXCEPTION,
                                query.context()))
                ));
    }
}
