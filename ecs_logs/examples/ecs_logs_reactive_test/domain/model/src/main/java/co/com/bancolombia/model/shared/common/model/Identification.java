package co.com.bancolombia.model.shared.common.model;

import co.com.bancolombia.model.shared.common.value.DocumentNumber;
import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public class Identification {

    private final DocumentNumber documentNumber;

    public Identification(String documentNumber, String className) {
        nonNullCheck(documentNumber);
        this.documentNumber = new DocumentNumber(documentNumber, className);
    }

    private void nonNullCheck(Object... values) {
        if (Arrays.stream(values).anyMatch(Objects::isNull)) {
            throw new BusinessException(ConstantBusinessException.MISSING_SEARCH_CLIENT_PARAMETERS_AGGREGATE);
        }
    }
}
