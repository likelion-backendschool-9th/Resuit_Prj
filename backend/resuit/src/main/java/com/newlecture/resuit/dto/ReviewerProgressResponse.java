package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.BadgeStatus;
import java.time.LocalDateTime;

public record ReviewerProgressResponse(long adoptedCount, long distinctRequesters,
        long bestReviewCount, boolean reviewerQualified, boolean excellentQualified,
        BadgeStatus reviewerStatus, BadgeStatus excellentStatus,
        LocalDateTime nextEvaluationAt, LocalDateTime continuousSince, int streakQuarters) {
}
