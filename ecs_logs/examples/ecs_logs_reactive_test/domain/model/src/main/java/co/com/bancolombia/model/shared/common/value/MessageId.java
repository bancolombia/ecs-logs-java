package co.com.bancolombia.model.shared.common.value;

import java.util.UUID;

public class MessageId extends AbstractUUID {
    public MessageId(String value) {
        super(value);
    }

    @Override
    public UUID getValue() {
        return value;
    }
}
