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
    void testMaskSensitiveData_IDCard() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"idCard\":\"123456789012345678\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // ID card should be masked: 123****89012345678
        assertTrue(result.contains("123****89012345678"));
        assertFalse(result.contains("123456789012345678"));
    }

    @Test
    void testMaskSensitiveData_BankCard() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"cardNumber\":\"1234567890123456\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        // Bank card should be masked: 123****890123456
        assertTrue(result.contains("123****890123456"));
        assertFalse(result.contains("1234567890123456"));
    }

    @Test
    void testMaskSensitiveData_Password() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"password\":\"secret123\",\"username\":\"user\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Password should be masked: "password":"***"
        assertTrue(result.contains("\"password\":\"***\""));
        assertFalse(result.contains("secret123"));
    }

    @Test
    void testMaskSensitiveData_Token() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\"user\":\"admin\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Token should be masked: "token":"***"
        assertTrue(result.contains("\"token\":\"***\""));
        assertFalse(result.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));
    }

    @Test
    void testMaskSensitiveData_MultipleSensitiveFields() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"password\":\"secret123\",\"token\":\"abc123\",\"phone\":\"13812345678\",\"email\":\"user@example.com\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // All sensitive fields should be masked
        assertTrue(result.contains("\"password\":\"***\""));
        assertTrue(result.contains("\"token\":\"***\""));
        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("user***@example.com"));
        assertFalse(result.contains("secret123"));
        assertFalse(result.contains("abc123"));
        assertFalse(result.contains("13812345678"));
        assertFalse(result.contains("user@example.com"));
    }

    @Test
    void testMaskSensitiveData_NullInput() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String result = (String) maskMethod.invoke(aspect, (String) null);
        assertNull(result);
    }

    @Test
    void testMaskSensitiveData_EmptyInput() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String result = (String) maskMethod.invoke(aspect, "");
        assertEquals("", result);
    }

    @Test
    void testMaskSensitiveData_NoSensitiveData() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"name\":\"张三\",\"age\":30,\"city\":\"北京\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Should remain unchanged
        assertEquals(input, result);
    }

    @Test
    void testMaskSensitiveData_InvalidPhoneNumber() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"phone\":\"12345\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Should remain unchanged as it doesn't match phone pattern
        assertEquals(input, result);
    }

    @Test
    void testMaskSensitiveData_InvalidEmail() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"email\":\"invalid-email\",\"name\":\"张三\"}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Should remain unchanged as it doesn't match email pattern
        assertEquals(input, result);
    }

    @Test
    void testMaskSensitiveData_ComplexJson() throws Exception {
        Method maskMethod = RequestLoggingAspect.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskMethod.setAccessible(true);
        
        String input = "{\"user\":{\"name\":\"张三\",\"phone\":\"13812345678\",\"email\":\"user@example.com\"},\"order\":{\"id\":123,\"token\":\"secret123\"}}";
        String result = (String) maskMethod.invoke(aspect, input);
        
        // Should mask all sensitive data
        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("user***@example.com"));
        assertTrue(result.contains("\"token\":\"***\""));
        assertFalse(result.contains("13812345678"));
        assertFalse(result.contains("user@example.com"));
        assertFalse(result.contains("secret123"));
    }

    @Test
    void testTruncateIfNeeded_NullInput() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        String result = (String) truncateMethod.invoke(aspect, (String) null);
        assertNull(result);
    }

    @Test
    void testTruncateIfNeeded_ShortString() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        String input = "Short string";
        String result = (String) truncateMethod.invoke(aspect, input);
        assertEquals(input, result);
    }

    @Test
    void testTruncateIfNeeded_LongString() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        // Create a string longer than MAX_LOG_LENGTH (1024)
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            longString.append("a");
        }
        String input = longString.toString();
        String result = (String) truncateMethod.invoke(aspect, input);
        
        assertEquals(1024, result.length());
        assertTrue(result.endsWith("...[TRUNCATED]"));
    }

    @Test
    void testTruncateIfNeeded_ExactLength() throws Exception {
        Method truncateMethod = RequestLoggingAspect.class.getDeclaredMethod("truncateIfNeeded", String.class);
        truncateMethod.setAccessible(true);
        
        // Create a string exactly 1024 characters long
        StringBuilder exactString = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            exactString.append("a");
        }
        String input = exactString.toString();
        String result = (String) truncateMethod.invoke(aspect, input);
        
        assertEquals(input, result);
    }

    @Test
    void testExtractResultSummary_NullResult() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        String result = (String) extractMethod.invoke(aspect, (Object) null);
        assertEquals("null", result);
    }

    @Test
    void testExtractResultSummary_ResultObject() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        Result<String> resultObj = Result.success("test data");
        String result = (String) extractMethod.invoke(aspect, resultObj);
        assertTrue(result.contains("code: SUCCESS"));
        assertTrue(result.contains("message: Operation completed successfully"));
        assertTrue(result.contains("data: test data"));
    }

    @Test
    void testExtractResultSummary_ResponseEntity() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        Result<String> resultObj = Result.success("test data");
        ResponseEntity<Result<String>> responseEntity = ResponseEntity.ok(resultObj);
        String result = (String) extractMethod.invoke(aspect, responseEntity);
        assertTrue(result.contains("code: SUCCESS"));
        assertTrue(result.contains("message: Operation completed successfully"));
        assertTrue(result.contains("data: test data"));
    }

    @Test
    void testExtractResultSummary_PlainObject() throws Exception {
        Method extractMethod = RequestLoggingAspect.class.getDeclaredMethod("extractResultSummary", Object.class);
        extractMethod.setAccessible(true);
        
        String plainObject = "test string";
        String result = (String) extractMethod.invoke(aspect, plainObject);
        
        assertEquals("\"test string\"", result);
    }
} 