package co.com.bancolombia.core.coustumerservice.client.search.application;

import co.com.bancolombia.model.coustumerservice.client.search.gateway.SearchClientGateway;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClient;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.cqrs.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SearchClientAdapter implements SearchClientGateway {

    private final Map<String, SearchClient> clients = Map.of(
            "12368542", new SearchClient("12368542", "Jorge", "1", "Cal"),
            "5462145", new SearchClient("5462145", "Jane", "2", "Medellin"),
            "65841236", new SearchClient("65841236", "Pablo", "3", "Bogota"),
            "8459312", new SearchClient("8459312","Maria","4","Pereira")
            );

    @Override
    public Mono<SearchClient> getClientByIdentification(Query<String, ContextData> query) {
        var identifier = query.payload();
        return Mono.justOrEmpty(clients.get(identifier));
    }
}
