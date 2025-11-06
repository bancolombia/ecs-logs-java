package co.com.bancolombia.ecs.domain.middleware;

import co.com.bancolombia.ecs.application.LoggerEcs;
import co.com.bancolombia.ecs.domain.model.AbstractMiddlewareEcsLog;
import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.request.LogRequest;

import java.util.Objects;

public class MiddlewareEcsRequest extends AbstractMiddlewareEcsLog {
    private AbstractMiddlewareEcsLog next;

    @Override
    protected void process(Object request, String service) {
        if (request instanceof LogRequest requestInfo) {
            LogRecord<String, String> logRecord = new LogRecord<>();
            if(Objects.nonNull(requestInfo.getMessageId())) {
                logRecord.setMessageId(requestInfo.getMessageId());
            }
            logRecord.setService(service);
            logRecord.setLevel(LogRecord.Level.INFO);
            logRecord.setConsumer(requestInfo.getConsumer());

            LogRecord.AdditionalInfo<String, String> additionalInfo = buildAdditionalInfo(requestInfo);
            logRecord.setAdditionalInfo(additionalInfo);

            setErrorInformation(requestInfo, logRecord);

            LoggerEcs.print(logRecord);
        } else if (next != null) {
            next.handler(request, service);
        }
    }

    private void setErrorInformation(LogRequest requestInfo, LogRecord<String, String> logRecord) {
        if (Objects.nonNull(requestInfo.getError())) {
            var error = requestInfo.getError();
            LogRecord.ErrorLog<String, String> optionalMap = new LogRecord.ErrorLog<>();
            var logError = new LogRecord.ErrorLog<String, String>();
            if (error instanceof BusinessExceptionECS exp) {
                optionalMap.setOptionalInfo(exp.getOptionalInfo());
                logError.setOptionalInfo(optionalMap.getOptionalInfo());
                logError.setDescription(exp.getConstantBusinessException().getInternalMessage());
                logError.setMessage(exp.getConstantBusinessException().getMessage());
                logError.setType(exp.getConstantBusinessException().getLogCode());
            } else {
                logError.setDescription(error.getMessage());
                logError.setMessage(error.getMessage());
                logError.setType(error.getClass().getName());
            }
            logRecord.setError(logError);
            logRecord.setLevel(LogRecord.Level.ERROR);
        }
    }

    private LogRecord.AdditionalInfo<String, String> buildAdditionalInfo(LogRequest requestInfo) {
        LogRecord.AdditionalInfo<String, String> additionalInfo = new LogRecord.AdditionalInfo<>();
        additionalInfo.setHeaders(requestInfo.getHeaders());
        additionalInfo.setRequestBody(requestInfo.getRequestBody());
        additionalInfo.setResponseBody(requestInfo.getResponseBody());
        additionalInfo.setResponseResult(requestInfo.getResponseResult());
        additionalInfo.setResponseCode(requestInfo.getResponseCode());
        additionalInfo.setMethod(requestInfo.getMethod());
        additionalInfo.setUri(requestInfo.getUrl());
        return additionalInfo;
    }

    @Override
    public AbstractMiddlewareEcsLog setNext(AbstractMiddlewareEcsLog next) {
        this.next = next;
        return this;
    }
}

