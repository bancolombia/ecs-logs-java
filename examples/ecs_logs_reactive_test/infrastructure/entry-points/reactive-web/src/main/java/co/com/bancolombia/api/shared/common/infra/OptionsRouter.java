package co.com.bancolombia.api.shared.common.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OptionsRouter {

    @Bean
    public RouterFunction<ServerResponse> rootOptions() {
        return route(OPTIONS("/**"), request -> ServerResponse.ok().build());
    }
}
