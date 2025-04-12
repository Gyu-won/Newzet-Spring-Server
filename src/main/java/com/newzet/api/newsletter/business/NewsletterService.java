package com.newzet.api.newsletter.business;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newzet.api.common.DistributedRequestMerger;
import com.newzet.api.newsletter.business.dto.NewsletterEntityDto;
import com.newzet.api.newsletter.domain.Newsletter;
import com.newzet.api.newsletter.domain.NewsletterStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NewsletterService {

	private static final String NEWSLETTER_DOMAIN_PREFIX = "newsletter:domain:";
	private static final Long MUTEX_WAIT_TIME = 5000L;
	private static final Long MUTEX_LEASE_TIME = 3000L;

	private final NewsletterRepository newsletterRepository;
	private final NewsletterCacheService cacheService;
	private final DistributedRequestMerger<Newsletter> distributedRequestMerger;

	public Newsletter findOrCreateNewsletter(String name, String domain, String mailingList) {
		return cacheService.findOnCache(domain)
			.orElseGet(() ->
				distributedRequestMerger.merge(
					NEWSLETTER_DOMAIN_PREFIX + domain, MUTEX_WAIT_TIME, MUTEX_LEASE_TIME,
					() -> {return findOrCreateNewsletter(name, domain, mailingList);},
					() -> {return findOrCreateNewsletter(name, domain, mailingList);})
				);
	}

	private Newsletter findOrCreateByDomainOrMailingListInDatabase(String name, String domain,
		String mailingList) {
		Newsletter newsletter = newsletterRepository
			.findByDomainOrMailingList(NEWSLETTER_DOMAIN_PREFIX + domain, mailingList)
			.map(NewsletterEntityDto::toDomain)
			.orElseGet(() -> createNewsletter(name, domain, mailingList));
		cacheService.saveOnCache(NEWSLETTER_DOMAIN_PREFIX + domain, newsletter);
		return newsletter;
	}

	private Newsletter createNewsletter(String name, String domain, String mailingList) {
		try {
			return newsletterRepository.save(name, domain, mailingList, NewsletterStatus.UNREGISTERED.name())
				.toDomain();
		} catch (DuplicateKeyException e) {
			return newsletterRepository.findByDomainOrMailingList(domain, mailingList)
				.map(NewsletterEntityDto::toDomain)
				.orElseThrow(() -> new IllegalStateException("Insert 실패 후 조회도 실패"));
		}
	}
}
