package com.ecommerce.api.aspect;

import com.ecommerce.api.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingAspectTest {

    private RequestLoggingAspect aspect;

    @Mock
    private org.aspectj.lang.ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aspect = new RequestLoggingAspect();
    }

    @Test
    void testMaskSensitiveData_PhoneNumber() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"phone\":\"13812345678\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Phone number should be masked: 138****5678
        assertTrue(result.contains("138****5678"));
        assertFalse(result.contains("13812345678"));
    }

    @Test
    void testMaskSensitiveData_Email() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"email\":\"user@example.com\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Email should be masked: user***@example.com
        assertTrue(result.contains("user***@example.com"));
        assertFalse(result.contains("user@example.com"));
    }

    @Test
    void testMaskSensitiveData_Password() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"password\":\"secret123\",\"username\":\"admin\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Password should be masked
        assertTrue(result.contains("\"password\":\"***\""));
        assertFalse(result.contains("secret123"));
    }

    @Test
    void testTruncateIfNeeded_ShortContent() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        String shortContent = "This is a short message";
        String result = (String) truncateMethod.invoke(aspect, shortContent);
        
        assertEquals(shortContent, result);
    }

    @Test
    void testTruncateIfNeeded_LongContent() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        // Create a string longer than 256 characters
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longContent.append("a");
        }
        
        String result = (String) truncateMethod.invoke(aspect, longContent.toString());
        
        // Should be truncated to 256 characters
        assertEquals(256, result.length());
        assertTrue(result.endsWith("...[TRUNCATED]"));
    }

    @Test
    void testTruncateIfNeeded_NullContent() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        String result = (String) truncateMethod.invoke(aspect, (String) null);
        
        assertNull(result);
    }

    @Test
    void testExtractResultSummary_ResultObject() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        Result<String> result = Result.successWithMessage("Success", "test data");
        
        String summary = (String) extractMethod.invoke(aspect, result);
        
        assertTrue(summary.contains("code: SUCCESS"));
        assertTrue(summary.contains("message: Success"));
        assertTrue(summary.contains("data: \"test data\""));
    }

    @Test
    void testExtractResultSummary_ResponseEntity() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        Result<String> result = Result.successWithMessage("Success", "test data");
        ResponseEntity<Result<String>> responseEntity = ResponseEntity.ok(result);
        
        String summary = (String) extractMethod.invoke(aspect, responseEntity);
        
        assertTrue(summary.contains("code: SUCCESS"));
        assertTrue(summary.contains("message: Success"));
    }

    @Test
    void testExtractResultSummary_Null() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        String summary = (String) extractMethod.invoke(aspect, (Object) null);
        
        assertEquals("null", summary);
    }
} 