package co.com.bancolombia.model.shared.common.value;

import java.util.UUID;

public abstract class AbstractUUID {

    protected final UUID value;

    protected AbstractUUID(String value) {
        if (value == null || value.isBlank()) {
            var message = "UUID value for %s. It must not be blank";
            throw new IllegalArgumentException(message.formatted(this.getClass().getSimpleName()));
        }
        this.value = getUUIDValue(value);
    }

    private UUID getUUIDValue(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            var message = "Invalid UUID value for %s:%s. It must be a valid UUID";
            throw new IllegalArgumentException(message.formatted(this.getClass().getSimpleName(), value));
        }
    }

    public abstract UUID getValue();

    @Override
    public String toString() {
        var className = this.getClass().getSimpleName();
        var formatted = "%s{value='%s'}";
        return formatted.formatted(className, value.toString());
    }
}
