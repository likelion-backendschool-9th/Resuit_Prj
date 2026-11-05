package com.newlecture.resuit.service;

import com.newlecture.resuit.repository.IntegrationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BlogRssScheduler {
    private static final Logger log = LoggerFactory.getLogger(BlogRssScheduler.class);
    private final IntegrationRepository integrations;
    private final BlogRssCollector collector;

    public BlogRssScheduler(IntegrationRepository integrations, BlogRssCollector collector) {
        this.integrations = integrations;
        this.collector = collector;
    }

    @Scheduled(cron = "0 15 * * * *", zone = "Asia/Seoul")
    public void collect() {
        integrations.findByProviderAndConnectedTrue("BLOG").forEach(integration -> {
            try {
                collector.collect(integration.getId());
            } catch (RuntimeException e) {
                log.warn("RSS 수집 실패: integrationId={}", integration.getId(), e);
            }
        });
    }
}
