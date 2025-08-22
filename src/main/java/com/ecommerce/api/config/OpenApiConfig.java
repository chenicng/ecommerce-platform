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
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * OpenAPI Configuration for Swagger UI
 * 
 * This configuration sets up comprehensive API documentation with:
 * - API metadata and contact information
 * - Version management with multiple groups (v1, v2, public)
 * - Security scheme configuration
 * - Organized API tags
 */
@Configuration
public class OpenApiConfig {

    @Bean
    @Primary
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

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApi -> openApi.info(v1ApiInfo()))
                .build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("v2")
                .pathsToMatch("/api/v2/**")
                .addOpenApiCustomizer(openApi -> openApi.info(v2ApiInfo()))
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/health/**", "/api/version/**")
                .addOpenApiCustomizer(openApi -> openApi.info(publicApiInfo()))
                .build();
    }

    private Info apiInfo() {
        return new Info()
                .title("E-commerce Platform API")
                .description("Comprehensive REST API for e-commerce platform with DDD architecture. " +
                           "Supports multi-version API, user management, merchant operations, product catalog, " +
                           "and order processing with daily batch settlement.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-commerce Platform Team")
                    .email("api-support@ecommerce.com")
                    .url("https://github.com/chenicng/ecommerce-platform")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }

    private Info v1ApiInfo() {
        return new Info()
                .title("E-commerce Platform API - V1")
                .description("API Version 1 - Core functionality including user management, merchant operations, " +
                           "product catalog, and order processing. This is the stable production version.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-commerce Platform Team")
                    .email("api-support@ecommerce.com")
                    .url("https://github.com/chenicng/ecommerce-platform")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }

    private Info v2ApiInfo() {
        return new Info()
                .title("E-commerce Platform API - V2")
                .description("API Version 2 - Simple enhancements over V1 with additional response fields. " +
                           "V2 includes timestamp information (createdAt, lastUpdated) and enhanced user status " +
                           "to demonstrate API versioning capabilities.")
                .version("2.0.0")
                .contact(new Contact()
                    .name("E-commerce Platform Team")
                    .email("api-support@ecommerce.com")
                    .url("https://github.com/chenicng/ecommerce-platform")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }

    private Info publicApiInfo() {
        return new Info()
                .title("E-commerce Platform API - Public")
                .description("Public API endpoints for system health monitoring and version information. " +
                           "These endpoints do not require authentication and are suitable for load balancers and monitoring systems.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-commerce Platform Team")
                    .email("api-support@ecommerce.com")
                    .url("https://github.com/chenicng/ecommerce-platform")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }
} 