package com.ecommerce.api.controller;

import com.ecommerce.api.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("System is healthy"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("ecommerce-platform"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    void healthCheck_WithAcceptHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void healthCheck_WithXmlAcceptHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable()); // Should return 406 since only JSON is supported
    }

    @Test
    void healthCheck_WithInvalidMethod() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void healthCheck_WithInvalidPath() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthCheck_ResponseStructure() throws Exception {
        // When
        String response = mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        Result<Map<String, Object>> result = objectMapper.readValue(response, 
            objectMapper.getTypeFactory().constructParametricType(Result.class, Map.class));
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals("System is healthy", result.getMessage());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertEquals("UP", data.get("status"));
        assertEquals("ecommerce-platform", data.get("service"));
        assertEquals("1.0.0", data.get("version"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void healthCheck_TimestampFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timestamp").isString())
                .andExpect(jsonPath("$.data.timestamp").value(org.hamcrest.Matchers.matchesPattern(
                    "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")));
    }

    @Test
    void healthCheck_ResponseHeaders() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(header().exists("Date"));
    }

    @Test
    void healthCheck_WithQueryParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health?format=json&detailed=true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void healthCheck_WithCustomHeaders() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health")
                .header("X-Request-ID", "test-123")
                .header("X-Client-Version", "1.0.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void healthCheck_ResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Health check should be fast (less than 100ms)
        assertTrue(responseTime < 100, "Health check response time should be less than 100ms, but was " + responseTime + "ms");
    }

    @Test
    void healthCheck_MultipleRequests() throws Exception {
        // Test multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/health")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"));
        }
    }

    @Test
    void healthCheck_DataConsistency() throws Exception {
        // When
        String response1 = mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String response2 = mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        Result<Map<String, Object>> result1 = objectMapper.readValue(response1, 
            objectMapper.getTypeFactory().constructParametricType(Result.class, Map.class));
        Result<Map<String, Object>> result2 = objectMapper.readValue(response2, 
            objectMapper.getTypeFactory().constructParametricType(Result.class, Map.class));

        // Status, service, and version should be consistent
        assertEquals(result1.getData().get("status"), result2.getData().get("status"));
        assertEquals(result1.getData().get("service"), result2.getData().get("service"));
        assertEquals(result1.getData().get("version"), result2.getData().get("version"));
        
        // Timestamps should be different
        assertNotEquals(result1.getData().get("timestamp"), result2.getData().get("timestamp"));
    }
} 