package co.com.bancolombia.ecs.model.management;

public class DefaultErrorManagement implements ErrorManagement {

    public static final int DEFAULT_ERROR_CODE = 500;

    @Override
    public Integer getStatus() {
        return DEFAULT_ERROR_CODE;
    }

    @Override
    public String getMessage() {
        return "Ha ocurrido un error interno en el servicio.";
    }

    @Override
    public String getErrorCode() {
        return "ER500-01";
    }

    @Override
    public String getInternalMessage() {
        return "";
    }

    @Override
    public String getLogCode() {
        return "";
    }
}

