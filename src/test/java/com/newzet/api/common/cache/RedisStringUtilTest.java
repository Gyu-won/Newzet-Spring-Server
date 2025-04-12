package com.newzet.api.common.cache;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newzet.api.common.objectMapper.OptionalObjectMapper;
import com.newzet.api.common.redis.RedisStringUtil;
import com.newzet.api.config.RedisTestContainerConfig;

@DataRedisTest
@Import({ObjectMapper.class, OptionalObjectMapper.class, RedisStringUtil.class})
@ExtendWith(RedisTestContainerConfig.class)
class RedisStringUtilTest {

	@Autowired
	private RedisStringUtil redisStringUtil;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@AfterEach
	void cleanUp() {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	@Test
	public void set_whenCachedValueNoExists() {
		//Given
		String key = "testKey";
		String value = "testValue";
		long ttl = 60000L;

		assertFalse(redisStringUtil.get(key, String.class).isPresent());

		//When
		redisStringUtil.set(key, value, ttl);

		//Then
		assertTrue(redisStringUtil.get(key, String.class).isPresent());
	}

	@Test
	public void set_whenCachedValueExists_updateTTL() throws InterruptedException {
		//Given
		String key = "testKey";
		String value = "testValue";
		long ttl = 1000L;

		redisStringUtil.set(key, value, ttl);
		assertTrue(redisStringUtil.get(key, String.class).isPresent());

		//When
		redisStringUtil.set(key, value, 3000L);

		// Then
		Thread.sleep(1000L);
		assertTrue(redisStringUtil.get(key, String.class).isPresent());
	}

	@Test
	public void get_whenCachedValueNoExists_returnOptionalEmpty() {
		//Given
		String key = "testKey";
		String value = "testValue";
		long ttl = 60000L;

		//When
		Optional<String> returnValue = redisStringUtil.get(key, String.class);

		//Then
		assertEquals(Optional.empty(), returnValue);
	}

	@Test
	public void get_whenCachedValueExist_returnValue() {
		//Given
		String key = "testKey";
		String value = "testValue";
		long ttl = 3000L;

		//When
		redisStringUtil.set(key, value, ttl);
		Optional<String> returnValue = redisStringUtil.get(key, String.class);

		//Then
		assertTrue(returnValue.isPresent());
		assertEquals(value, returnValue.get());
	}
}
