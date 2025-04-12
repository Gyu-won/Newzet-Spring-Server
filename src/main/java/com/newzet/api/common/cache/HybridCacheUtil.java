package com.newzet.api.common.cache;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.newzet.api.common.cache.local.LocalCacheUtil;
import com.newzet.api.common.redis.RedisServerException;
import com.newzet.api.common.redis.RedisStringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class HybridCacheUtil implements CacheUtil {
	private final RedisStringUtil redisStringUtil;
	private final LocalCacheUtil localCacheUtil;

	@Override
	public <T> Optional<T> get(String key, Class<T> classType) {
		try {
			return redisStringUtil.get(key, classType);
		} catch (RedisServerException e) {
			return localCacheUtil.get(key, classType);
		}
	}

	@Override
	public <T> void set(String key, T object, long ttl) {
		try {
			redisStringUtil.set(key, object, ttl);
		} catch (RedisServerException e) {
			localCacheUtil.set(key, object, ttl);
		}
	}
}
