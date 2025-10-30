package co.com.bancolombia.ecs.domain.middleware;


import co.com.bancolombia.ecs.application.LoggerEcs;
import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.domain.model.LogRecord;

public class MiddlewareEcsExcp extends AbstractMiddlewareEcsLog {
    private AbstractMiddlewareEcsLog next;

    @Override
    public void process(Object request,
                        String service) {
        if (request instanceof Exception exp) {

            var logError = new LogRecord.ErrorLog<String, String>();
            logError.setDescription(exp.getMessage());
            logError.setMessage(exp.getMessage());
            logError.setType(exp.getClass().getName());

            var logExp = new LogRecord<String, String>();
            logExp.setError(logError);
            logExp.setLevel(LogRecord.Level.ERROR);
            logExp.setService(service);

            LoggerEcs.print(logExp);
        } else if (next != null) {
            next.handler(request, service);
        }
    }


    @Override
    public AbstractMiddlewareEcsLog setNext(AbstractMiddlewareEcsLog next) {
        this.next = next;
        return this;
    }
}
