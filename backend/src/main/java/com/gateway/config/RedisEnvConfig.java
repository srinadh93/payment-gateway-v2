package com.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class RedisEnvConfig {

    @Value("${spring.redis.host:${SPRING_REDIS_HOST:redis}}")
    private String redisHost;

    @Value("${spring.redis.port:${SPRING_REDIS_PORT:6379}}")
    private String redisPort;

    public String getRedisHost() {
        String envHost = System.getenv("SPRING_REDIS_HOST");
        return envHost != null ? envHost : redisHost;
    }

    public String getRedisPort() {
        String envPort = System.getenv("SPRING_REDIS_PORT");
        return envPort != null ? envPort : redisPort;
    }
}
