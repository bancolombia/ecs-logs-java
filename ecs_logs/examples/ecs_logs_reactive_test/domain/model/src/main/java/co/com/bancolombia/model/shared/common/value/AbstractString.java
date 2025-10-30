package co.com.bancolombia.model.shared.common.value;

import java.util.regex.Pattern;

public abstract class AbstractString {
    protected final String value;

    protected AbstractString(String inputValue) {
        if (isEmpty(inputValue)) {
            var message = "Invalid value for %s. It must not be blank";
            throwError(message.formatted(this.getClass().getSimpleName()));
        }
        this.value = inputValue.trim();
    }

    protected AbstractString(String inputValue, Pattern pattern) {
        this(inputValue);
        if (!pattern.matcher(this.value).matches()) {
            var message = "Invalid value for %s:%s. It must match the pattern %s";
            throwError(message.formatted(this.getClass().getSimpleName(), value, pattern.toString()));
        }
    }

    protected AbstractString(String inputValue, int maxLength) {
        this(inputValue);
        if (this.value.length() > maxLength) {
            var message = "Invalid value for %s:%s. Its length must be less than or equal to %d";
            throwError(message.formatted(this.getClass().getSimpleName(), value, maxLength));
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    private void throwError(String message) {
        throw new IllegalArgumentException(message);
    }

    public abstract String getValue();

    @Override
    public String toString() {
        var className = this.getClass().getSimpleName();
        var formatted = "%s{value='%s'}";
        return formatted.formatted(className, value);
    }
}
