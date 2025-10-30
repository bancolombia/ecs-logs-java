package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcsPropertiesConfigTest {

    private ServiceProperties serviceProps;
    private SensitiveRequestProperties reqProps;
    private SensitiveResponseProperties resProps;
    private EcsPropertiesConfig config;

    @BeforeEach
    void setUp() {
        serviceProps = new ServiceProperties();
        serviceProps.setName("ecs-test");

        reqProps = new SensitiveRequestProperties();
        reqProps.setDelimiter(",");
        reqProps.setShow(Boolean.TRUE);
        reqProps.setAllowHeaders("Authorization,Content-Type");
        reqProps.setFields("password,secret");
        reqProps.setPatterns(".*secret.*");
        reqProps.setReplacement("***");
        reqProps.setExcludedPaths("/excluded,/health");

        resProps = new SensitiveResponseProperties();
        resProps.setDelimiter(";");
        resProps.setShow(Boolean.TRUE);
        resProps.setFields("token;sessionId");
        resProps.setPatterns(".*tokenValue.*");
        resProps.setReplacement("***");

        config = new EcsPropertiesConfig(serviceProps, reqProps, resProps);
    }

    @Test
    void testShouldParseRequestPropertiesCorrectly() {
        assertEquals("ecs-test", config.getServiceName());
        assertTrue(config.getSensitiveRequestFields().contains("password"));
        assertEquals(1, config.getSensitiveRequestPatterns().size());
        assertEquals("***", config.getSensitiveRequestReplacement());
        assertTrue(config.getExcludedPaths().contains("/excluded"));
    }

    @Test
    void testShouldParseResponsePropertiesCorrectly() {
        assertTrue(config.getSensitiveResponseFields().contains("token"));
        assertEquals(1, config.getSensitiveResponsePatterns().size());
        assertEquals("***", config.getSensitiveResponseReplacement());
    }

    @Test
    void testShouldParseRequestPropertiesShowFalse() {
        reqProps.setShow(Boolean.FALSE);
        resProps.setShow(Boolean.FALSE);
        config = new EcsPropertiesConfig(serviceProps, reqProps, resProps);
        assertEquals("ecs-test", config.getServiceName());
    }

    @Test
    void testShouldParseRequestPropertiesShowRequest() {
        reqProps.setShow(Boolean.TRUE);
        resProps.setShow(Boolean.FALSE);
        reqProps.setExcludedPaths(null);
        reqProps.setAllowHeaders(null);
        config = new EcsPropertiesConfig(serviceProps, reqProps, resProps);
        assertEquals("ecs-test", config.getServiceName());
        assertTrue(config.getExcludedPaths().contains("/actuator"));
        assertTrue(config.getAllowRequestHeaders().contains("message-id"));
    }

    @Test
    void testShouldParseRequestPropertiesShowResponse() {
        reqProps.setShow(Boolean.FALSE);
        resProps.setShow(Boolean.TRUE);
        reqProps.setDelimiter(null);
        reqProps.setExcludedPaths(null);
        reqProps.setAllowHeaders(null);
        config = new EcsPropertiesConfig(serviceProps, reqProps, resProps);
        assertEquals("ecs-test", config.getServiceName());
        assertTrue(config.getExcludedPaths().contains("/actuator"));
        assertTrue(config.getAllowRequestHeaders().contains("message-id"));
    }

    @Test
    void testShowRequestWithPropertiesEmpty() {
        reqProps.setShow(Boolean.TRUE);
        resProps.setShow(Boolean.FALSE);
        reqProps.setDelimiter("");
        reqProps.setExcludedPaths("");
        reqProps.setAllowHeaders("");
        config = new EcsPropertiesConfig(serviceProps, reqProps, resProps);
        assertEquals("ecs-test", config.getServiceName());
        assertTrue(config.getExcludedPaths().contains("/actuator"));
        assertTrue(config.getAllowRequestHeaders().contains("message-id"));
    }
}