package com.business.project.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator serviceRoutes(RouteLocatorBuilder routes) {
        return routes.routes()
                .route("task-service", route -> route
                        .path("/api/tasks/**")
                        .uri("lb://task-service"))
                .route("notification-service", route -> route
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                .build();
    }
}
