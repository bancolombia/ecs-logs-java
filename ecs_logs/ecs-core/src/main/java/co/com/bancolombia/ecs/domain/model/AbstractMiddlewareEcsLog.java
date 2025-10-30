package co.com.bancolombia.ecs.domain.model;

public abstract class AbstractMiddlewareEcsLog {

    private AbstractMiddlewareEcsLog next;

    public void handler(Object request, String service) {
        process(request, service);
        if (next != null) {
            next.handler(request, service);
        }
    }

    protected abstract void process(Object request, String service);

    public AbstractMiddlewareEcsLog setNext(AbstractMiddlewareEcsLog next) {
        this.next = next;
        return this;
    }
}
