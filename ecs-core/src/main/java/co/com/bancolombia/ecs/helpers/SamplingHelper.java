package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.infra.config.SamplingConfig;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Log4j2
@UtilityClass
public class SamplingHelper {

    private static Map<String, SamplingConfig.SamplingRule> rules = Collections.emptyMap();
    private static final Map<String, LongAdder> counters = new ConcurrentHashMap<>();
    private static final String INIT_MESSAGE = "{} sampling rules have been loaded successfully";
    private static final String START_CODE_40X = "40";
    private static final int MIN_ERROR_CODE_PARTS = 2;
    private static boolean samplingEnabled = true;

    public static void init(Map<String, SamplingConfig.SamplingRule> rulesMap) {
        if (rulesMap != null) {
            log.info(INIT_MESSAGE, rulesMap.size());
            rules = rulesMap;
        }
    }

    public static void setSamplingEnabled(boolean enabled) {
        samplingEnabled = enabled;
    }

    public static void reset() {
        rules = Collections.emptyMap();
        counters.clear();
        samplingEnabled = true;
    }

    private static void incrementCounter(String key) {
        counters.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    private static long getCount(String key) {
        return counters.getOrDefault(key, new LongAdder()).sum();
    }

    public static boolean validatePrint(LogRecord<String, String> ex) {
        if (!samplingEnabled || ex.getAdditionalInfo() == null) {
            return true;
        }
        String uri = ex.getAdditionalInfo().getUri();
        String responseCode = ex.getAdditionalInfo().getResponseCode();
        if (uri == null || responseCode == null) {
            return false;
        }
        return evaluateSamplingRule(ex, uri, responseCode);
    }

    private static boolean evaluateSamplingRule(LogRecord<String, String> ex, String uri, String responseCode) {
        String key = buildKey(ex, uri, responseCode);
        if (!rules.containsKey(key)) {
            return true;
        }
        var rule = rules.get(key);
        int cycle = rule.getShowCount() + rule.getSkipCount();
        incrementCounter(key);
        long current = getCount(key);
        long position = (current - 1) % cycle;
        if (current >= cycle) {
            counters.put(key, new LongAdder());
        }
        return position < rule.getShowCount();
    }

    private static String buildKey(LogRecord<String, String> ex, String uri, String responseCode) {
        if (responseCode.startsWith(START_CODE_40X)) {
            String errorType = ex.getError() != null ? ex.getError().getType() : null;
            if (errorType != null) {
                return uri + "|" + getErrorCode(errorType);
            }
        }
        return uri + "|" + responseCode;
    }

    private String getErrorCode(String errorCode){
        String[] parts = errorCode.split("-");
        var result = "";
        if (parts.length >= MIN_ERROR_CODE_PARTS) {
            result = parts[0] + "-" + parts[1];
        }
        return result;
    }

}
