package co.com.bancolombia.ecs.domain.model;

import lombok.Getter;

@Getter
public enum ExceptionLevel {
    BUSINESS_EXCEPTION_ECS("BusinessExceptionECS"),
    EXCEPTION("Exception"),
    THROWABLE("Throwable");

    private final String levelString;

    ExceptionLevel(String levelString) {
        this.levelString = levelString;
    }

    public static ExceptionLevel toExceptionLevelIgnoreCase(String value) {
        if (value != null) {
            for (ExceptionLevel exceptionLevel : ExceptionLevel.values()) {
                if (exceptionLevel.levelString.equalsIgnoreCase(value.trim())) {
                    return exceptionLevel;
                }
            }
        }
        return null;
    }
}
