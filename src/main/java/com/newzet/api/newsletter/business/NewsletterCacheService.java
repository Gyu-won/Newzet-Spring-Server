package com.newzet.api.newsletter.business;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newzet.api.common.cache.CacheUtil;
import com.newzet.api.newsletter.business.dto.NewsletterCacheDto;
import com.newzet.api.newsletter.domain.Newsletter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterCacheService {

	private static final Long CACHE_DURATION = 1000 * 60 * 5L;
	private static final String CACHE_PREFIX = "cache:";

	private final CacheUtil cacheUtil;

	public Optional<Newsletter> findOnCache(String key) {
		return cacheUtil.get(CACHE_PREFIX + key, NewsletterCacheDto.class)
			.map(NewsletterCacheDto::toDomain);
	}

	public void saveOnCache(String key, Newsletter newsletter) {
		cacheUtil.set(CACHE_PREFIX + key, newsletter.toCacheDto(), CACHE_DURATION);
	}
}
