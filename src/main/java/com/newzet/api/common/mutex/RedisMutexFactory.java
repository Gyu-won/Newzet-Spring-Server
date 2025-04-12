package com.newzet.api.common.mutex;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMutexFactory implements MutexFactory{

	private static final long MUTEX_ACQUISITION_INTERVAL_MS = 300;
	private static final String MUTEX_PREFIX = "mutex:";
	private static final String MUTEX_VALUE = "locked";

	private final MutexUtil mutexUtil;

	@Override
	public void tryMutex(String key, long waitTime, long leaseTime) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + waitTime;

		while (System.currentTimeMillis() < endTime) {
			boolean acquired = mutexUtil.setMutex(MUTEX_PREFIX + key, MUTEX_VALUE, leaseTime);
			if (acquired) return;

			try {
				Thread.sleep(MUTEX_ACQUISITION_INTERVAL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		throw new MutexAcquisitionException();
	}

	@Override
	public void release(String key) {
		mutexUtil.delete(MUTEX_PREFIX + key);
	}
}

