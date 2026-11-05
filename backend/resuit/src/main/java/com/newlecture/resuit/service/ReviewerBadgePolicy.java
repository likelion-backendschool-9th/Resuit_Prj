package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.BadgeStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 최근 두 분기의 채택 활동을 평가하는 리뷰어 배지 규칙. */
public final class ReviewerBadgePolicy {

    private ReviewerBadgePolicy() {
    }

    public record Activity(long adoptedCount, long distinctRequesters, long bestReviewCount) {
    }

    public enum Level {
        NONE,
        REVIEWER,
        EXCELLENT_REVIEWER
    }

    /** 직전 두 분기(6개월) 평가 구간. 끝은 가장 최근 분기 시작일 00:00. */
    public record EvaluationWindow(LocalDateTime from, LocalDateTime to) {
    }

    public static boolean qualifies(Level level, Activity activity) {
        return switch (level) {
            case NONE -> false;
            case REVIEWER -> activity.adoptedCount() >= 5 && activity.distinctRequesters() >= 3;
            case EXCELLENT_REVIEWER -> activity.adoptedCount() >= 15
                    && activity.distinctRequesters() >= 8 && activity.bestReviewCount() >= 1;
        };
    }

    public static Level targetLevel(Activity activity) {
        if (qualifies(Level.EXCELLENT_REVIEWER, activity)) {
            return Level.EXCELLENT_REVIEWER;
        }
        if (qualifies(Level.REVIEWER, activity)) {
            return Level.REVIEWER;
        }
        return Level.NONE;
    }

    /**
     * 단일 배지 전이. EXPIRED는 중도에 ACTIVE로 올리지 않는다(유예 중 복구만).
     * 강등 오케스트레이션은 ReviewerBadgeService가 담당한다.
     */
    public static BadgeStatus evaluate(BadgeStatus current, boolean qualified, boolean renewalDue) {
        if (qualified) {
            if (current == BadgeStatus.EXPIRED) {
                return BadgeStatus.EXPIRED;
            }
            return BadgeStatus.ACTIVE;
        }
        if (!renewalDue) {
            return current;
        }
        return switch (current) {
            case ACTIVE -> BadgeStatus.GRACE;
            case GRACE, EXPIRED -> BadgeStatus.EXPIRED;
        };
    }

    /**
     * 분기 평가일에는 그날 00:00을 끝으로, 그 외에는 이번 분기 시작일을 끝으로 한 6개월 창.
     * progress·채택 직후 재평가·분기 잡이 같은 경계를 쓰게 한다.
     */
    public static EvaluationWindow windowEndingAt(LocalDateTime at, boolean quarterEvaluation) {
        LocalDate day = at.toLocalDate();
        LocalDateTime to = quarterEvaluation || isQuarterStart(day)
                ? day.atStartOfDay()
                : currentQuarterStart(day);
        return new EvaluationWindow(to.minusMonths(6), to);
    }

    public static LocalDateTime currentQuarterStart(LocalDate date) {
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), month, 1).atStartOfDay();
    }

    public static LocalDateTime nextQuarterStart(LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3;
        int nextQuarter = quarter + 1;
        int year = date.getYear() + (nextQuarter == 4 ? 1 : 0);
        int month = (nextQuarter % 4) * 3 + 1;
        return LocalDate.of(year, month, 1).atStartOfDay();
    }

    public static boolean isQuarterStart(LocalDate date) {
        return date.getDayOfMonth() == 1 && (date.getMonthValue() - 1) % 3 == 0;
    }

    /** continuous_since부터 지난 분기 수(최소 0). */
    public static int streakQuarters(LocalDateTime continuousSince, LocalDateTime at) {
        if (continuousSince == null) {
            return 0;
        }
        LocalDate from = continuousSince.toLocalDate();
        LocalDate to = at.toLocalDate();
        int months = (to.getYear() - from.getYear()) * 12 + (to.getMonthValue() - from.getMonthValue());
        return Math.max(0, months / 3);
    }
}
