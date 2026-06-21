package com.business.project.taskmanagement.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiter notificationRateLimiter(
            @Value("${services.notification.rate-limiter.limit-for-period}") int limitForPeriod,
            @Value("${services.notification.rate-limiter.limit-refresh-period}") Duration limitRefreshPeriod,
            @Value("${services.notification.rate-limiter.timeout-duration}") Duration timeoutDuration
    ) {
        io.github.resilience4j.ratelimiter.RateLimiterConfig config = io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .build();

        return RateLimiter.of("notification-service", config);
    }
}
