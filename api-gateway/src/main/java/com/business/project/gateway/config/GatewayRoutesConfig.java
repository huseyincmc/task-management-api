package com.business.project.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.time.Duration;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RouteLocator serviceRoutes(
            RouteLocatorBuilder routes,
            RedisRateLimiter gatewayRateLimiter,
            KeyResolver clientIpKeyResolver
    ) {
        return routes.routes()
                .route("task-service", route -> route
                        .path("/api/tasks/**")
                        .filters(filters -> filters
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(gatewayRateLimiter)
                                        .setKeyResolver(clientIpKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("task-service-gateway-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/task-service")))
                        .metadata(CONNECT_TIMEOUT_ATTR, 2000)
                        .metadata(RESPONSE_TIMEOUT_ATTR, Duration.ofSeconds(3))
                        .uri("lb://task-service"))
                .route("notification-service", route -> route
                        .path("/api/notifications/**")
                        .filters(filters -> filters
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(gatewayRateLimiter)
                                        .setKeyResolver(clientIpKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("notification-service-gateway-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/notification-service")))
                        .metadata(CONNECT_TIMEOUT_ATTR, 2000)
                        .metadata(RESPONSE_TIMEOUT_ATTR, Duration.ofSeconds(3))
                        .uri("lb://notification-service"))
                .build();
    }

    @Bean
    public RedisRateLimiter gatewayRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> {
            String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return reactor.core.publisher.Mono.just(forwardedFor.split(",")[0].trim());
            }

            if (exchange.getRequest().getRemoteAddress() == null) {
                return reactor.core.publisher.Mono.just("unknown-client");
            }

            return reactor.core.publisher.Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        };
    }
}
