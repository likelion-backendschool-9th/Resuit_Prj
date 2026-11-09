package com.newlecture.resuit.scheduler;

import com.newlecture.resuit.service.GithubContributionStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "resuit.github.contribution-sync-enabled", havingValue = "true", matchIfMissing = true)
public class GithubContributionStatScheduler {

    private static final Logger log = LoggerFactory.getLogger(GithubContributionStatScheduler.class);

    private final GithubContributionStatService statService;

    public GithubContributionStatScheduler(GithubContributionStatService statService) {
        this.statService = statService;
    }

    @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
    public void syncAll() {
        // 회원마다 따로 처리해서, 한 명이 실패해도 나머지는 계속 갱신한다.
        for (String username : statService.findSyncTargets()) {
            try {
                statService.sync(username);
            } catch (RuntimeException e) {
                log.warn("GitHub 기여 수 갱신 실패: username={}", username, e);
            }
        }
    }
}
