package co.com.bancolombia.api.coustumerservice.client.search.infra;

import co.com.bancolombia.api.coustumerservice.client.search.domain.SearchClientRequest;
import co.com.bancolombia.model.coustumerservice.client.search.model.SearchClientIdentification;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@UtilityClass
public class TransformRequest {

    public static Mono<SearchClientIdentification> fromRequest(ServerRequest serverRequest,
                                                               ContextData contextData) {
        return serverRequest.bodyToMono(SearchClientRequest.class)
                .filter(request -> request.getData() != null)
                .switchIfEmpty(Mono.error(
                        new BusinessException(ConstantBusinessException.MISSING_REQUIRED_DATA_SEARCH_PARAMETERS)))
                .map(requestBody -> mapperToModel(requestBody, serverRequest))
                .onErrorMap(BusinessException.class, e ->
                        new BusinessException((ConstantBusinessException) e.getConstantBusinessException(),
                                e.getOptionalInfo(), contextData))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new BusinessException(ConstantBusinessException.INVALID_FORMAT_FIELDS_RETRIEVE_PARAMETERS,
                                e.getMessage(), contextData));
    }

    private static SearchClientIdentification mapperToModel(SearchClientRequest bodyRequest,
                                                            ServerRequest request) {
        var data = bodyRequest.getData();
        var identification = data.getIdentification();

        return new SearchClientIdentification(identification.getNumber());
    }
}
