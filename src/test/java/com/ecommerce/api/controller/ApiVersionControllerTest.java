package com.ecommerce.api.controller;

import com.ecommerce.api.config.ApiVersionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API version controller test
 */
@WebMvcTest(ApiVersionController.class)
class ApiVersionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetVersionInfo() throws Exception {
        mockMvc.perform(get("/api/version/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(ApiVersionConfig.DEFAULT_VERSION))
                .andExpect(jsonPath("$.supportedVersions").isArray())
                .andExpect(jsonPath("$.strategy.urlPathVersioning").value(true))
                .andExpect(jsonPath("$.compatibility.backwardCompatible").value(true));
    }
    
    @Test
    void testGetVersionExamples() throws Exception {
        mockMvc.perform(get("/api/version/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlPathVersioning").exists())
                .andExpect(jsonPath("$.urlPathVersioning.v1_user_create").value("POST /api/v1/users"))
                .andExpect(jsonPath("$.urlPathVersioning.v2_user_create").value("POST /api/v2/users"));
    }
    
    @Test
    void testCheckVersionCompatibility_SupportedVersion() throws Exception {
        mockMvc.perform(get("/api/version/compatibility/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedVersion").value("v1"))
                .andExpect(jsonPath("$.isSupported").value(true))
                .andExpect(jsonPath("$.currentVersion").value(ApiVersionConfig.DEFAULT_VERSION));
    }
    
    @Test
    void testCheckVersionCompatibility_UnsupportedVersion() throws Exception {
        mockMvc.perform(get("/api/version/compatibility/v99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedVersion").value("v99"))
                .andExpect(jsonPath("$.isSupported").value(false))
                .andExpect(jsonPath("$.recommendation").exists());
    }
    
    @Test
    void testCheckVersionCompatibility_WithoutVPrefix() throws Exception {
        mockMvc.perform(get("/api/version/compatibility/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedVersion").value("v1"))
                .andExpect(jsonPath("$.isSupported").value(true));
    }
} 