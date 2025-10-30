package co.com.bancolombia.model.shared.common.value;

import java.util.UUID;

public class XRequestId extends AbstractUUID {
    public XRequestId(String value) {
        super(value);
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
