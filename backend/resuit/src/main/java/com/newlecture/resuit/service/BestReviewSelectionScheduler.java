package com.newlecture.resuit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "resuit.reviews.selection-enabled", havingValue = "true", matchIfMissing = true)
public class BestReviewSelectionScheduler {
    private static final Logger log = LoggerFactory.getLogger(BestReviewSelectionScheduler.class);
    private final BestReviewSelectionService service;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void selectLatestCompletedPeriods() {
        var today = LocalDate.now(clock.withZone(BestReviewSelectionService.ZONE));
        run(BestReviewSelectionService.Period.WEEKLY,
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1));
        run(BestReviewSelectionService.Period.MONTHLY, today.withDayOfMonth(1).minusMonths(1));
    }

    private void run(BestReviewSelectionService.Period period, LocalDate start) {
        try {
            var result = service.select(period, start);
            log.info("Best review selection: period={}, start={}, selected={}, alreadyCompleted={}",
                period, start, result.selected(), result.alreadyCompleted());
        } catch (RuntimeException ex) {
            log.error("Best review selection failed: period={}, start={}", period, start, ex);
        }
    }
}
