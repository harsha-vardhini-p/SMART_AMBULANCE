package com.lifegrid.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lifeGridOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeGrid API - Smart Ambulance Dispatch & Hospital Routing")
                        .description("Simulation engine for emergency medical response triage, shortest-path ambulance routing (Dijkstra), " +
                                "and capacity-aware hospital allocation. \n\n" +
                                "**DISCLAIMER: Everything is a simulation for college demonstration. Travel times are estimates based on baseline traffic models.**")
                        .version("1.0.0")
                        .contact(new Contact().name("LifeGrid Engineering Team").email("support@lifegrid.local"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
    }
}
