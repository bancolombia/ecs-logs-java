package co.com.bancolombia.api.coustumerservice.client.search.application.validation;

import co.com.bancolombia.api.shared.common.domain.HeaderConstant;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@UtilityClass
public class HeadersValidatorSearchParametersClient {

    public static Mono<ContextData> validateHeaders(ServerRequest request) {
        var messageId = request.headers().firstHeader(HeaderConstant.MESSAGE_ID_LOWER.value());
        var consumerAcronym = request.headers().firstHeader(HeaderConstant.CODE.value());
        var idRelation = request.headers().firstHeader(HeaderConstant.RELATION_ID.value());
        var aidCreator = request.headers().firstHeader(HeaderConstant.AID_CREATOR_LOWER.value());

        if (isNullOrEmpty(messageId, consumerAcronym, idRelation, aidCreator)) {
            return Mono.error(new BusinessException(ConstantBusinessException.MISSING_HEADER_SEARCH_CLIENT_PARAMETERS));
        }

        try {
            var xRequestId = request.headers().firstHeader(HeaderConstant.X_REQUEST_ID.value());
            return Mono.just(new ContextData(messageId, xRequestId, consumerAcronym));
        } catch (IllegalArgumentException e) {
            return Mono.error(new BusinessException(ConstantBusinessException.INVALID_HEADERS_SEARCH_PARAMETERS,
                    e.getMessage()));
        }
    }

    private static boolean isNullOrEmpty(String... values) {
        return Arrays.stream(values).anyMatch(value -> value == null || value.isEmpty());
    }
}
