package co.com.bancolombia.ecs.domain.middleware;


import co.com.bancolombia.ecs.application.LoggerEcs;
import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;

public class MiddlewareEcsBusiness extends AbstractMiddlewareEcsLog {
    private AbstractMiddlewareEcsLog next;

    @Override
    public void process(Object request,
                        String service) {

        if (request instanceof BusinessExceptionECS exp) {

            LogRecord.ErrorLog<String, String> optionalMap = new LogRecord.ErrorLog<>();
            optionalMap.setOptionalInfo(exp.getOptionalInfo());

            var messageId = exp.getMetaInfo().getMessageId();

            var logError = new LogRecord.ErrorLog<String, String>();
            logError.setOptionalInfo(optionalMap.getOptionalInfo());
            logError.setDescription(exp.getConstantBusinessException().getInternalMessage());
            logError.setMessage(exp.getConstantBusinessException().getMessage());
            logError.setType(exp.getConstantBusinessException().getLogCode());

            var logExp = new LogRecord<String, String>();
            logExp.setError(logError);
            logExp.setLevel(LogRecord.Level.ERROR);
            logExp.setService(service);
            logExp.setMessageId(messageId);

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
