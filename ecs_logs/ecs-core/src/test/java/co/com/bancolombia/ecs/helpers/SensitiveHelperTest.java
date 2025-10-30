package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveHelperTest {


    @Test
    void shouldInitRulesCorrectly() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"firstName", "lastName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.5);
        rule.setEnabled(true);

        Map<String, List<SensitiveRulesConfig.SensitiveDataRule>> rulesMap = Map.of(
                "/test/endpoint", List.of(rule)
        );

        SensitiveHelper.init(rulesMap);

        String jsonInput = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertNotEquals(jsonInput, result);
        assertTrue(result.contains("Ju"));
        assertTrue(result.contains("Pé"));
    }

    @Test
    void shouldInitWithNullMap() {
        SensitiveHelper.init(null);

        String jsonInput = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldReturnOriginalJsonWhenNoRulesConfigured() {
        String jsonInput = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldReturnOriginalJsonWhenNoApplicableRules() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/other/endpoint");
        rule.setFieldPaths(new String[]{"firstName"});
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/other/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldApplyPartialMasking() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"nombre"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.2);
        rule.setMaskingChar("*");
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"nombre\":\"PRUEBANOMBRE1\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("PRU**********"));
    }

    @Test
    void shouldApplyFullMasking() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"password"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setMaskingChar("*");
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"password\":\"secretPassword123\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("***************"));
    }

    @Test
    void shouldApplyCustomMasking() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"email"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.CUSTOM);
        rule.setCustomMask("[PROTECTED]");
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"email\":\"user@example.com\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("[PROTECTED]"));
        assertFalse(result.contains("user@example.com"));
    }

    @Test
    void shouldRemoveFieldWhenRemoveMasking() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"token"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.REMOVE);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"token\":\"abc123\",\"username\":\"john\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertFalse(result.contains("token"));
        assertFalse(result.contains("abc123"));
        assertTrue(result.contains("username"));
        assertTrue(result.contains("john"));
    }

    @Test
    void shouldApplyComplexFieldPaths() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"additionalInfo.responseBody.data.flowResponse[*].naturalPersonInformationDetail.firstName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.35);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"additionalInfo\":{\"responseBody\":{\"data\":{\"flowResponse\":[{\"naturalPersonInformationDetail\":{\"firstName\":\"Carlos\"}}]}}}}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("Car"));
        assertFalse(result.contains("Carlos"));
    }

    @Test
    void shouldIgnoreDisabledRules() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"firstName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(false);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":\"Juan\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldApplyMultipleRulesForSameUri() {
        SensitiveRulesConfig.SensitiveDataRule rule1 = new SensitiveRulesConfig.SensitiveDataRule();
        rule1.setUriPattern("/test/endpoint");
        rule1.setFieldPaths(new String[]{"firstName"});
        rule1.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule1.setEnabled(true);

        SensitiveRulesConfig.SensitiveDataRule rule2 = new SensitiveRulesConfig.SensitiveDataRule();
        rule2.setUriPattern("/test/endpoint");
        rule2.setFieldPaths(new String[]{"email"});
        rule2.setMaskingType(SensitiveRulesConfig.MaskingType.CUSTOM);
        rule2.setCustomMask("[HIDDEN]");
        rule2.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule1, rule2)));

        String jsonInput = "{\"firstName\":\"Juan\",\"email\":\"juan@test.com\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("[HIDDEN]"));
        assertFalse(result.contains("juan@test.com"));
        assertFalse(result.contains("Juan"));
    }

    @Test
    void shouldReturnOriginalJsonWhenParsingFails() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"firstName"});
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String invalidJson = "{\"firstName\":\"Juan\",}";
        String result = SensitiveHelper.filterSensitiveData(invalidJson, "/test/endpoint");

        assertEquals(invalidJson, result);
    }

    @Test
    void shouldNotMaskNonExistentField() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"nonExistentField"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldNotMaskNonTextField() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"age", "isStudent"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":\"Juan\",\"age\":30,\"isStudent\":true}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("\"age\":30"));
        assertTrue(result.contains("\"isStudent\":true"));
    }

    @Test
    void shouldMaskFieldInsideArray() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"email"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.5);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"users\":[{\"email\":\"test1@example.com\"},{\"email\":\"test2@example.com\"}]}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertFalse(result.contains("test1@example.com"));
        assertFalse(result.contains("test2@example.com"));
        assertTrue(result.contains("test1"));
        assertTrue(result.contains("test2"));
    }

    @Test
    void shouldNotMaskWhenExpectedArrayIsNotAnArray() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"additionalInfo.responseBody.data.flowResponse[*].naturalPersonInformationDetail.firstName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.35);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"additionalInfo\":{\"responseBody\":{\"data\":{\"flowResponse\":{\"name\":\"notAnArray\"}}}}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldMaskArrayElementsWhenPathEndsWithArrayWildcard() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"data.items[*]"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setMaskingChar("*");
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"data\":{\"items\":[\"value1\",\"value2\",\"value3\"]}}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertFalse(result.contains("value1"));
        assertTrue(result.contains("***************"));
    }

    @Test
    void shouldNotApplyMaskIfTargetNodeIsNotObjectOrFieldDoesNotExist() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"id.username"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"id\":12345,\"user\":{\"username\":\"testUser\"}}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldNotApplyMaskIfTargetNodeIsNotTextual() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"details.age"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);

        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"details\":{\"age\":30}}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldNotMaskWhenValueIsNull() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"firstName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":null,\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldNotMaskWhenValueIsEmpty() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"firstName"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.FULL);
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"firstName\":\"\",\"lastName\":\"Pérez\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldReturnOriginalValueWhenCustomMaskIsNullAndMaskingTypeIsCustom() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"dataField"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.CUSTOM);
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"dataField\":\"originalValue\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertEquals(jsonInput, result);
    }

    @Test
    void shouldReturnOriginalValueWhenMaskingTypeIsRemove() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"dataField"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.REMOVE);
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"dataField\":\"valueToBeRemoved\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertFalse(result.contains("dataField"));
    }

    @Test
    void shouldMaskSingleCharacterValue() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"singleChar"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.5);
        rule.setMaskingChar("*");
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"singleChar\":\"a\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("\"singleChar\":\"*\""));
    }

    @Test
    void shouldHandleEmptyValueInPartialMask() {
        SensitiveRulesConfig.SensitiveDataRule rule = new SensitiveRulesConfig.SensitiveDataRule();
        rule.setUriPattern("/test/endpoint");
        rule.setFieldPaths(new String[]{"emptyValue"});
        rule.setMaskingType(SensitiveRulesConfig.MaskingType.PARTIAL);
        rule.setVisibilityPercentage(0.5);
        rule.setMaskingChar("*");
        rule.setEnabled(true);
        SensitiveHelper.init(Map.of("/test/endpoint", List.of(rule)));

        String jsonInput = "{\"emptyValue\":\"\"}";
        String result = SensitiveHelper.filterSensitiveData(jsonInput, "/test/endpoint");

        assertTrue(result.contains("\"emptyValue\":\"\""));
    }

}