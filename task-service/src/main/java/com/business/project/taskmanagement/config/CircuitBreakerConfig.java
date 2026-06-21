package com.business.project.taskmanagement.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreaker notificationCircuitBreaker(
            @Value("${services.notification.circuit-breaker.failure-rate-threshold}") float failureRateThreshold,
            @Value("${services.notification.circuit-breaker.minimum-number-of-calls}") int minimumNumberOfCalls,
            @Value("${services.notification.circuit-breaker.sliding-window-size}") int slidingWindowSize,
            @Value("${services.notification.circuit-breaker.wait-duration-in-open-state}") Duration waitDurationInOpenState,
            @Value("${services.notification.circuit-breaker.permitted-number-of-calls-in-half-open-state}") int permittedNumberOfCallsInHalfOpenState
    ) {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .slidingWindowSize(slidingWindowSize)
                .waitDurationInOpenState(waitDurationInOpenState)
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .build();

        return CircuitBreaker.of("notification-service", config);
    }
}
