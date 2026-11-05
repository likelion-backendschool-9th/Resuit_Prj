package com.newlecture.resuit.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReviewerBadgeScheduler {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
    private final ReviewerBadgeService service;

    public ReviewerBadgeScheduler(ReviewerBadgeService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 5 0 1 1,4,7,10 *", zone = "Asia/Seoul")
    public void evaluateQuarter() {
        service.evaluateQuarter(LocalDateTime.now(BUSINESS_ZONE));
    }
}
