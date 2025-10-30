package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.domain.model.LogRecord;

import java.util.HashMap;
import java.util.Map;

public class LoggerEcsTestHelper {
    public boolean shouldPrint(int index, int show, int skip) {
        int cycle = show + skip;
        int position = index % cycle;
        return position < show;
    }

    public static LogRecord<String, String> generateTestLogRecord() {
        LogRecord<String, String> logRecord = new LogRecord<>();
        logRecord.setService("TestService");
        logRecord.setLevel(LogRecord.Level.INFO);
        logRecord.setMessageId("test-message-id");
        logRecord.setConsumer("test-consumer");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, String> request = new HashMap<>();
        request.put("testKey", "testValue");

        Map<String, String> response = new HashMap<>();
        response.put("result", "success");

        LogRecord.AdditionalInfo<String, String> additionalInfo = LogRecord.AdditionalInfo.<String, String>builder()
                .method("POST")
                .uri("/test/endpoint")
                .headers(headers)
                .requestBody(request)
                .responseBody(response)
                .responseCode("200")
                .responseResult("OK")
                .build();

        logRecord.setAdditionalInfo(additionalInfo);

        return logRecord;
    }

}

