package co.com.bancolombia.model.shared.common.value;

import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;

public class Acronym {

    private final String value;

    private static final int MAX_ACRONYM_LENGTH = 3;

    public Acronym(String value, String className) {

        if(value == null) {
            throw new BusinessException(ConstantBusinessException.ACRONYM_NULL, className);
        }
        if (value.length() > MAX_ACRONYM_LENGTH) {
            throw new BusinessException(ConstantBusinessException.INVALID_ACRONYM, className);
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
