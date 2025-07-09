package com.ecommerce.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Swagger UI
 * 
 * This configuration sets up comprehensive API documentation with:
 * - API metadata and contact information
 * - Version management
 * - Security scheme configuration
 * - Organized API tags
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:8080").description("Development Server"),
                    new Server().url("https://api.ecommerce.com").description("Production Server")
                ))
                .security(List.of(
                    new SecurityRequirement().addList("Bearer Authentication")
                ))
                .components(new Components()
                    .addSecuritySchemes("Bearer Authentication", 
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT token for API authentication")
                    )
                )
                .tags(List.of(
                    new Tag().name("Health Check").description("System health monitoring endpoints"),
                    new Tag().name("API Version").description("API version information and compatibility"),
                    new Tag().name("User Management").description("User registration, authentication and account management"),
                    new Tag().name("Merchant Management").description("Merchant registration, product management and income tracking"),
                    new Tag().name("Product & Purchase").description("Product browsing, purchasing and order management"),
                    new Tag().name("User Management V2").description("Enhanced user management with additional features (API v2)")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("E-commerce Platform API")
                .description("Comprehensive REST API for e-commerce platform with DDD architecture. " +
                           "Supports multi-version API, user management, merchant operations, product catalog, " +
                           "and order processing with real-time settlement.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-commerce Platform Team")
                    .email("api-support@ecommerce.com")
                    .url("https://github.com/chenlicong0821/ecommerce-platform")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }
} 