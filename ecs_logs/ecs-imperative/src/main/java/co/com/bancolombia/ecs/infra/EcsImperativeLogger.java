package co.com.bancolombia.ecs.infra;

import co.com.bancolombia.ecs.domain.middleware.MiddlewareEcsBusiness;
import co.com.bancolombia.ecs.domain.middleware.MiddlewareEcsExcp;
import co.com.bancolombia.ecs.domain.middleware.MiddlewareEcsRequest;
import co.com.bancolombia.ecs.domain.middleware.MiddlewareEcsTrow;
import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.model.request.LogRequest;

public final class EcsImperativeLogger {

    private EcsImperativeLogger() {
    }

    public static AbstractMiddlewareEcsLog build() {
        var ecsBusiness = new MiddlewareEcsBusiness();
        var ecsExp = new MiddlewareEcsExcp();
        var ecsTrow = new MiddlewareEcsTrow();
        var ecsRequest = new MiddlewareEcsRequest();
        return ecsRequest.setNext(ecsBusiness.setNext(ecsExp.setNext(ecsTrow)));
    }

    public static Throwable build(Throwable throwable,
                                  String service) {
        build().handler(throwable, service);
        return throwable;
    }

    public static void build(LogRequest request, String service) {
        build().handler(request, service);
    }
}
