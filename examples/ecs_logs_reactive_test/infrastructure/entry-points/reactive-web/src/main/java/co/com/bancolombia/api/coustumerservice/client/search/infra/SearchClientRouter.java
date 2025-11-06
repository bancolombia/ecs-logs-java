package co.com.bancolombia.api.coustumerservice.client.search.infra;

import co.com.bancolombia.api.coustumerservice.client.search.application.SearchClientHandler;
import co.com.bancolombia.api.shared.common.domain.Paths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class SearchClientRouter {

    @Bean
    public RouterFunction<ServerResponse> searchClient(SearchClientHandler handler){
        return route(GET(Paths.GET_CLIENT), handler::searchClient);
    }
}
