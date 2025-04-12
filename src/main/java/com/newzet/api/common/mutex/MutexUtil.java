package com.newzet.api.common.mutex;

public interface MutexUtil {
	public boolean setMutex(String key, String value, long ttl);

	public void delete(String key);
}
