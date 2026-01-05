package com.rev.app.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    @Primary
    fun cacheManager(connectionFactory: RedisConnectionFactory?): CacheManager {
        // Redis 연결이 실패하면 NoOpCacheManager 사용 (캐싱 없이 작동)
        return try {
            if (connectionFactory == null) {
                println("⚠️ Redis ConnectionFactory가 null입니다. 캐싱을 비활성화합니다.")
                return NoOpCacheManager()
            }
            // Redis 연결 테스트
            val connection = connectionFactory.connection
            connection.ping()
            connection.close()
            
            // Redis 사용 가능하면 RedisCacheManager 생성
            // ObjectMapper에 JSR310 모듈 추가하여 Instant 직렬화 지원
            val objectMapper = ObjectMapper().apply {
                registerModule(JavaTimeModule())
                // 알 수 없는 속성 무시
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 타입 정보를 포함하여 역직렬화 시 정확한 타입으로 복원
                // @JsonTypeInfo 어노테이션과 함께 사용하여 제네릭 타입 역직렬화 지원
                activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY
                )
            }
            
            val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 기본 TTL: 10분
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJackson2JsonRedisSerializer(objectMapper)
                    )
                )
                .disableCachingNullValues()

            // 캐시별 커스텀 설정
            val cacheConfigurations = mapOf(
                "threads" to defaultConfig.entryTtl(Duration.ofMinutes(5)), // 게시글 목록: 5분
                "threadDetail" to defaultConfig.entryTtl(Duration.ofMinutes(15)), // 게시글 상세: 15분
                "boards" to defaultConfig.entryTtl(Duration.ofHours(1)), // 게시판 목록: 1시간
                "userProfile" to defaultConfig.entryTtl(Duration.ofMinutes(30)), // 사용자 프로필: 30분
                "reactions" to defaultConfig.entryTtl(Duration.ofMinutes(5)), // 반응: 5분
                "bookmarks" to defaultConfig.entryTtl(Duration.ofMinutes(5)), // 북마크: 5분
            )

            println("✅ Redis 연결 성공. 캐싱을 활성화합니다.")
            RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build()
        } catch (e: Exception) {
            println("⚠️ Redis 연결 실패: ${e.message}. 캐싱을 비활성화하고 계속 진행합니다.")
            NoOpCacheManager()
        }
    }
}

