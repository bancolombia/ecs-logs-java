package co.com.bancolombia.model.coustumerservice.client.search.gateway;

import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClient;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.cqrs.Query;
import reactor.core.publisher.Mono;

public interface SearchClientGateway {
    Mono<SearchClient> getClientByIdentification(Query<String, ContextData> query);
}
