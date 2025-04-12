package com.newzet.api.common.mutex;

public interface MutexFactory {
	public void tryMutex(String key, long waitTime, long leaseTime);

	public void release(String key);
}
