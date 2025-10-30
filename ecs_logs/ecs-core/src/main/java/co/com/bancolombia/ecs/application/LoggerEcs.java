package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.helpers.SamplingHelper;
import co.com.bancolombia.ecs.helpers.SensitiveHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggerEcs {

    private LoggerEcs() {
    }

    public static void print(LogRecord<String, String> ex) {
        if (!SamplingHelper.validatePrint(ex)) {
            return;
        }

        String filteredJson = filterSensitiveDataIfNeeded(ex);

        switch (ex.getLevel()) {
            case DEBUG -> log.debug(filteredJson);
            case INFO -> log.info(filteredJson);
            case WARNING -> log.warn(filteredJson);
            case ERROR -> log.error(filteredJson);
            case FATAL -> log.fatal(filteredJson);
        }
    }

    private static String filterSensitiveDataIfNeeded(LogRecord<String, String> ex) {
        String uri = ex.getAdditionalInfo() != null ? ex.getAdditionalInfo().getUri() : null;
        if (uri != null) {
            return SensitiveHelper.filterSensitiveData(ex.toJson(), uri);
        }
        return ex.toJson();
    }
}
