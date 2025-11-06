package co.com.bancolombia.ecs.model.management;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BusinessExceptionECSTest {

    @Test
    void testConstructorWithStringMessage() {
        String message = "Test Exception";
        BusinessExceptionECS ex = new BusinessExceptionECS(message);
        assertEquals(message, ex.getMessage());
        assertNotNull(ex.getConstantBusinessException());
        assertNotNull(ex.getOptionalInfo());
        assertNotNull(ex.getMetaInfo());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementOnly() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS ex = new BusinessExceptionECS(err);
        assertEquals(err.getLogCode(), ex.getMessage());
        assertEquals(err, ex.getConstantBusinessException());
        assertNotNull(ex.getOptionalInfo());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementAndOptionalString() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS ex = new BusinessExceptionECS(err, "optional");
        assertEquals(err.getLogCode(), ex.getMessage());
        assertEquals("optional", ex.getOptionalInfo().get("OPTIONAL"));
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementAndOptionalNull() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS ex = new BusinessExceptionECS(err, (String) null);
        assertEquals(err.getLogCode(), ex.getMessage());
        assertEquals("", ex.getOptionalInfo().get("OPTIONAL"));
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementAndMetaInfo() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS.MetaInfo meta = BusinessExceptionECS.MetaInfo.builder().build();
        BusinessExceptionECS ex = new BusinessExceptionECS(err, meta);
        assertEquals(err.getLogCode(), ex.getMessage());
        assertEquals(meta.getMessageId(), ex.getMetaInfo().getMessageId());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementAndOptionalMap() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        Map<String, String> map = new HashMap<>();
        map.put("OPTIONAL", "value");
        BusinessExceptionECS ex = new BusinessExceptionECS(err, map);
        assertEquals("value", ex.getOptionalInfo().get("OPTIONAL"));
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementStringAndMetaInfo() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS.MetaInfo meta = BusinessExceptionECS.MetaInfo.builder().build();
        BusinessExceptionECS ex = new BusinessExceptionECS(err, "value", meta);
        assertEquals("value", ex.getOptionalInfo().get("OPTIONAL"));
        assertEquals(meta.getMessageId(), ex.getMetaInfo().getMessageId());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithErrorManagementOptionalNullAndMetaInfo() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS.MetaInfo meta = BusinessExceptionECS.MetaInfo.builder().build();
        BusinessExceptionECS ex = new BusinessExceptionECS(err, (String) null, meta);
        assertEquals("", ex.getOptionalInfo().get("OPTIONAL"));
        assertEquals(meta.getMessageId(), ex.getMetaInfo().getMessageId());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithAllParams() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        Map<String, String> map = Map.of("OPTIONAL", "v1");
        BusinessExceptionECS.MetaInfo meta = BusinessExceptionECS.MetaInfo.builder().build();
        BusinessExceptionECS ex = new BusinessExceptionECS(err, map, meta);

        assertEquals("v1", ex.getOptionalInfo().get("OPTIONAL"));
        assertEquals(meta.getMessageId(), ex.getMetaInfo().getMessageId());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithNullErrorManagementReturnsDefault() {
        BusinessExceptionECS ex = new BusinessExceptionECS(null, "value");
        assertEquals(ErrorManagement.DEFAULT_EXCEPTION.getLogCode(), ex.getMessage());
        assertEquals(ErrorManagement.DEFAULT_EXCEPTION, ex.getConstantBusinessException());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testConstructorWithNullMetaInfoReturnsGenerated() {
        ErrorManagement err = ErrorManagement.DEFAULT_EXCEPTION;
        BusinessExceptionECS ex = new BusinessExceptionECS(err, "val", null);
        assertNotNull(ex.getMetaInfo().getMessageId());
        assertNotNull(ex.getConstantBusinessException().getMessage());
        assertNotNull(ex.getConstantBusinessException().getErrorCode());
        assertNotNull(ex.getConstantBusinessException().getInternalMessage());
        assertNotNull(ex.getConstantBusinessException().getStatus());
        assertNotNull(ex.getConstantBusinessException().getLogCode());
    }

    @Test
    void testMetaInfoBuilderCreatesUniqueIds() {
        String id1 = BusinessExceptionECS.MetaInfo.builder().build().getMessageId();
        String id2 = BusinessExceptionECS.MetaInfo.builder().build().getMessageId();
        assertNotEquals(id1, id2);
    }

}