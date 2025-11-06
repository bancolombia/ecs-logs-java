package co.com.bancolombia.ecs.model.management;

import java.io.Serializable;

public interface ErrorManagement extends Serializable {
    ErrorManagement DEFAULT_EXCEPTION = new DefaultErrorManagement();

    Integer getStatus();

    String getMessage();

    String getErrorCode();

    String getInternalMessage();

    String getLogCode();
}
