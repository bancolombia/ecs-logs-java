package co.com.bancolombia.api.shared.common.domain.response;

import co.com.bancolombia.ecs.model.management.ErrorManagement;
import co.com.bancolombia.model.shared.exception.BusinessException;
import lombok.Data;

import java.util.List;

@Data
public class ErrorApiResponse {
    private Meta meta;
    private List<Error> errors;

    private ErrorApiResponse(Meta meta, BusinessException exception) {
        this.meta = meta;
        this.errors = getErrors(exception.getConstantBusinessException());
    }

    public static ErrorApiResponse build(BusinessException exception) {
        return new ErrorApiResponse(new Meta(exception.getMetaInfo().getMessageId()), exception);
    }

    private List<Error> getErrors(ErrorManagement exception) {
        return List.of(Error.builder().code(exception.getErrorCode()).detail(exception.getMessage()).build());
    }
}
