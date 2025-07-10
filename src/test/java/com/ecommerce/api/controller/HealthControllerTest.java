package com.ecommerce.api.controller;

import com.ecommerce.api.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for HealthController
 */
@WebMvcTest(HealthController.class)
class HealthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void health_ShouldReturnSuccessResponse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("System is healthy"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("ecommerce-platform"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }
    
    @Test
    void health_ShouldHaveCorrectContentType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
    
    @Test
    void health_ShouldReturnValidJsonStructure() throws Exception {
        // When & Then
        String response = mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Verify JSON can be parsed
        Result<Map<String, Object>> result = objectMapper.readValue(response, 
            objectMapper.getTypeFactory().constructParametricType(Result.class, Map.class));
        
        assert result.getCode().equals("SUCCESS");
        assert result.getMessage().equals("System is healthy");
        assert result.getData().containsKey("status");
        assert result.getData().containsKey("timestamp");
        assert result.getData().containsKey("service");
        assert result.getData().containsKey("version");
    }
    
    @Test
    void health_ShouldReturnCorrectHeaders() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }
    
    @Test
    void health_ShouldReturnCorrectStatusValue() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
    
    @Test
    void health_ShouldReturnCorrectServiceName() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.service").value("ecommerce-platform"));
    }
    
    @Test
    void health_ShouldReturnCorrectVersion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value("1.0.0"));
    }
    
    @Test
    void health_ShouldReturnTimestamp() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timestamp").exists())
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty());
    }
} 