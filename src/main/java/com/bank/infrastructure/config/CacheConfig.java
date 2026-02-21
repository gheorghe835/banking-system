package com.bank.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configurare ObjectMapper pentru serializarea corectă a datelor Java 8 time
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Configurare CacheManager pentru Redis
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Configurare default pentru toate cache-urile
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))                    // Expiră după 10 minute
                .disableCachingNullValues()                           // Nu salva valori null
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper())  // Folosește ObjectMapper-ul configurat
                        )
                );

        // Configurații specifice pentru diferite tipuri de cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache pentru exchangeRates - expiră mai repede (5 minute)
        cacheConfigurations.put("exchangeRates",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Cache pentru accounts - expiră după 15 minute
        cacheConfigurations.put("accounts",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Cache pentru customers - expiră după 30 minute
        cacheConfigurations.put("customers",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Cache pentru validations - expiră după 60 minute
        cacheConfigurations.put("validations",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}