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

    public static void init(Map<String, SamplingConfig.SamplingRule> rulesMap) {
        if (rulesMap != null) {
            log.info(INIT_MESSAGE, rulesMap.size());
            rules = rulesMap;
        }
    }

    public static void reset() {
        rules = Collections.emptyMap();
        counters.clear();
    }

    private static void incrementCounter(String key) {
        counters.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    private static long getCount(String key) {
        return counters.getOrDefault(key, new LongAdder()).sum();
    }

    public static boolean validatePrint(LogRecord<String, String> ex) {
        boolean result = false;
        if (ex.getAdditionalInfo() == null) return true;

        String uri = ex.getAdditionalInfo().getUri();
        String responseCode = ex.getAdditionalInfo().getResponseCode();
        if (uri != null && responseCode != null) {
            String key = responseCode.startsWith(START_CODE_40X)
                    ? uri + "|" + getErrorCode(ex.getError().getType())
                    : uri + "|" + responseCode;

            if (!rules.containsKey(key)) {
                return true;
            }

            SamplingConfig.SamplingRule rule = rules.get(key);
            int cycle = rule.getShowCount() + rule.getSkipCount();

            incrementCounter(key);
            long current = getCount(key);
            long position = (current - 1) % cycle;

            if (current >= cycle) {
                counters.put(key, new LongAdder());
            }

            result = position < rule.getShowCount();
        }

        return result;
    }

    private String getErrorCode(String errorCode){
        String[] parts = errorCode.split("-");
        String result = "";
        if (parts.length >= 2) {
            result = parts[0] + "-" + parts[1];
        }
        return result;
    }

}
