package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.ReviewerProgressResponse;
import com.newlecture.resuit.entity.BadgeStatus;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.repository.BadgeRepository;
import com.newlecture.resuit.repository.BestReviewRepository;
import com.newlecture.resuit.repository.ReviewerActivityRepository;
import com.newlecture.resuit.repository.UserBadgeRepository;
import com.newlecture.resuit.service.BadgeProgressService;
import com.newlecture.resuit.service.ReviewerBadgePolicy;
import com.newlecture.resuit.service.ReviewerBadgePolicy.EvaluationWindow;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BadgeProgressServiceImpl implements BadgeProgressService {
    private final ReviewerActivityRepository activities;
    private final BestReviewRepository bestReviews;
    private final BadgeRepository badges;
    private final UserBadgeRepository userBadges;

    @Override
    @Transactional(readOnly = true)
    public ReviewerProgressResponse getProgress(UUID userId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        EvaluationWindow window = ReviewerBadgePolicy.windowEndingAt(now, false);
        ReviewerBadgePolicy.Activity activity = new ReviewerBadgePolicy.Activity(
                activities.countAdopted(userId, window.from(), window.to()),
                activities.countDistinctRequesters(userId, window.from(), window.to()),
                bestReviews.countForReviewer(userId, window.from().toLocalDate(),
                        window.to().toLocalDate()));
        UserBadge streakSource = continuousSource(userId);
        LocalDateTime continuousSince = streakSource == null ? null : streakSource.getContinuousSince();
        return new ReviewerProgressResponse(activity.adoptedCount(),
                activity.distinctRequesters(), activity.bestReviewCount(),
                ReviewerBadgePolicy.qualifies(ReviewerBadgePolicy.Level.REVIEWER, activity),
                ReviewerBadgePolicy.qualifies(ReviewerBadgePolicy.Level.EXCELLENT_REVIEWER, activity),
                status(userId, "REVIEWER"), status(userId, "EXCELLENT_REVIEWER"),
                ReviewerBadgePolicy.nextQuarterStart(now.toLocalDate()),
                continuousSince,
                ReviewerBadgePolicy.streakQuarters(continuousSince, now));
    }

    private UserBadge continuousSource(UUID userId) {
        UserBadge excellent = owned(userId, "EXCELLENT_REVIEWER");
        if (excellent != null && excellent.getStatus() == BadgeStatus.ACTIVE
                && excellent.getContinuousSince() != null) {
            return excellent;
        }
        UserBadge reviewer = owned(userId, "REVIEWER");
        if (reviewer != null && reviewer.getStatus() == BadgeStatus.ACTIVE) {
            return reviewer;
        }
        return null;
    }

    private UserBadge owned(UUID userId, String code) {
        return badges.findByCode(code)
                .flatMap(b -> userBadges.findByIdUserIdAndIdBadgeId(userId, b.getId()))
                .orElse(null);
    }

    private BadgeStatus status(UUID userId, String code) {
        UserBadge owned = owned(userId, code);
        return owned == null ? null : owned.getStatus();
    }
}
