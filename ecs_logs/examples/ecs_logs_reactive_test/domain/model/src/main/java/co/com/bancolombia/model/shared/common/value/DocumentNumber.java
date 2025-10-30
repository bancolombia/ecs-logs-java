package co.com.bancolombia.model.shared.common.value;

import co.com.bancolombia.model.shared.exception.BusinessException;
import co.com.bancolombia.model.shared.exception.ConstantBusinessException;

import java.util.regex.Pattern;

public class DocumentNumber {

    private final String value;
    private static final Pattern DOCUEMNT_PATTERN = Pattern.compile("^[0-9A-Za-z]{1,15}$");

    public DocumentNumber(String value, String className) {

        if(value == null) {
            throw new BusinessException(ConstantBusinessException.DOCUMENT_NUMBER_NULL, className);
        }
        if (!DOCUEMNT_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ConstantBusinessException.INVALID_DOCUMENT_NUMBER, className);
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
