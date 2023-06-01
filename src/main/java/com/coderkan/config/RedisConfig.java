package com.coderkan.config;

import java.io.Serializable;
import java.time.Duration;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationTextPublisher;
import io.micrometer.observation.aop.ObservedAspect;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;
@Slf4j
//@Profile("!dev")
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableCaching
public class RedisConfig {


	@Value("${spring.redis.host}")
	private String redisHostName;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Value("${spring.redis.password}")
	private String redisPassword;

	@Value("${spring.cache.redis.time-to-live}")
	private int cacheTtl;

	@Value("${spring.cache.redis.cache-null-values}")
	private boolean cacheNull;


	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(50);
		poolConfig.setMaxTotal(50);
		JedisClientConfiguration.JedisClientConfigurationBuilder clientConfig = JedisClientConfiguration.builder();
		clientConfig.usePooling().poolConfig(poolConfig);
		JedisConnectionFactory jedisConnectionFactory;
		RedisStandaloneConfiguration redisServerConf = new RedisStandaloneConfiguration();
		log.info("redis host " + redisHostName);
		log.info("redis port " + String.valueOf(redisPort));
		redisServerConf.setHostName(redisHostName);
		redisServerConf.setPort(redisPort);
		if(redisPassword != null && !redisPassword.isEmpty()) {
			log.info("redis password " + redisPassword);
			redisServerConf.setPassword(RedisPassword.of(redisPassword));
		}
		jedisConnectionFactory = new JedisConnectionFactory(redisServerConf, clientConfig.build());
		return jedisConnectionFactory;
	}
	@Bean
	public RedisTemplate<String, Serializable> redisCacheTemplate(JedisConnectionFactory jedisConnectionFactory) {
		RedisTemplate<String, Serializable> template = new RedisTemplate<>();
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setConnectionFactory(jedisConnectionFactory);

		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		RedisCacheConfiguration redisCacheConfiguration = config
				.entryTtl(Duration.ofMinutes(cacheTtl))
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer()))
				;
		if (cacheNull) {
			redisCacheConfiguration.getAllowCacheNullValues();
		} else {
			redisCacheConfiguration.disableCachingNullValues();
		}
		RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(redisCacheConfiguration)
				.build();
		return redisCacheManager;
	}
	@Configuration
	public class ObservationTextPublisherConfiguration {

		private static final Logger log = LoggerFactory.getLogger(ObservationTextPublisherConfiguration.class);

		@Bean
		public ObservationHandler<Observation.Context> observationTextPublisher() {
			return new ObservationTextPublisher(log::info);
		}
	}
	@Configuration(proxyBeanMethods = false)
	public class ObserveConfiguration {

		@Bean
		ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
			return new ObservedAspect(observationRegistry);
		}

	}

	@PostConstruct
	public void clearCache() {
		System.out.println("In Clear Cache");
	/*	Jedis jedis = new Jedis(redisHost, redisPort, 1000);
		jedis.flushAll();
		jedis.close();
	 */
	}

}
