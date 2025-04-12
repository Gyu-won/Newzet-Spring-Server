package com.newzet.api.common;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.newzet.api.common.mutex.MutexFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedRequestMerger<T> {
	private final MutexFactory mutexFactory;
	private final Map<String, CompletableFuture<T>> futureMap = new ConcurrentHashMap<>();

	public T merge(String key, long waitTime, long leaseTime, Supplier<T> loader, Supplier<T> fallback) {
		CompletableFuture<T> future = futureMap.computeIfAbsent(key, k -> {
			CompletableFuture<T> f = new CompletableFuture<>();
			CompletableFuture.runAsync(() -> {
				try {
					mutexFactory.tryMutex(key, waitTime, leaseTime);
					try {
						T result = loader.get();
						f.complete(result);
					} finally {
						mutexFactory.release(key);
					}
				} catch (Exception e) {
					log.warn("mutex 획득 실패 key {}, fallback 적용", key);
					f.complete(fallback.get());
				} finally {
					futureMap.remove(key);
				}
			});
			return f;
		});

		return future.join();
	}
}
