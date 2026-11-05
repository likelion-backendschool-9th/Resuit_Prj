package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.entity.Badge;
import com.newlecture.resuit.entity.BadgeStatus;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.entity.UserBadgeHistory;
import com.newlecture.resuit.repository.BadgeRepository;
import com.newlecture.resuit.repository.BestReviewRepository;
import com.newlecture.resuit.repository.ReviewerActivityRepository;
import com.newlecture.resuit.repository.UserBadgeHistoryRepository;
import com.newlecture.resuit.repository.UserBadgeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.ReviewerBadgePolicy;
import com.newlecture.resuit.service.ReviewerBadgePolicy.EvaluationWindow;
import com.newlecture.resuit.service.ReviewerBadgePolicy.Level;
import com.newlecture.resuit.service.ReviewerBadgeService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewerBadgeServiceImpl implements ReviewerBadgeService {
    private final UserRepository users;
    private final BadgeRepository badges;
    private final UserBadgeRepository userBadges;
    private final UserBadgeHistoryRepository histories;
    private final ReviewerActivityRepository activities;
    private final BestReviewRepository bestReviews;

    @Override
    @Transactional
    public void evaluateQuarter(LocalDateTime at) {
        if (!ReviewerBadgePolicy.isQuarterStart(at.toLocalDate())) {
            throw new IllegalArgumentException("분기 시작일에만 분기 평가를 실행할 수 있습니다.");
        }
        for (User user : users.findAll()) {
            if ("USER".equals(user.getRole())) {
                evaluateUser(user, at, true);
            }
        }
    }

    @Override
    @Transactional
    public void refreshAfterActivity(UUID userId, LocalDateTime at) {
        User user = users.findById(userId).orElseThrow();
        if ("USER".equals(user.getRole())) {
            evaluateUser(user, at, false);
        }
    }

    private void evaluateUser(User user, LocalDateTime at, boolean quarterStart) {
        EvaluationWindow window = ReviewerBadgePolicy.windowEndingAt(at, quarterStart);
        LocalDateTime from = window.from();
        LocalDateTime to = window.to();
        UUID userId = user.getId();
        ReviewerBadgePolicy.Activity activity = new ReviewerBadgePolicy.Activity(
                activities.countAdopted(userId, from, to),
                activities.countDistinctRequesters(userId, from, to),
                bestReviews.countForReviewer(userId, from.toLocalDate(), to.toLocalDate()));

        Badge reviewerDef = definition("REVIEWER", "리뷰어");
        Badge excellentDef = definition("EXCELLENT_REVIEWER", "우수 리뷰어");
        UserBadge reviewer = findOwned(userId, reviewerDef.getId());
        UserBadge excellent = findOwned(userId, excellentDef.getId());
        Level target = ReviewerBadgePolicy.targetLevel(activity);

        if (quarterStart) {
            applyQuarterLadder(user, reviewerDef, excellentDef, reviewer, excellent, target, at, to);
        } else {
            applyMidCycleRecovery(user, reviewerDef, excellentDef, reviewer, excellent, target, at);
        }
    }

    private void applyQuarterLadder(User user, Badge reviewerDef, Badge excellentDef,
            UserBadge reviewer, UserBadge excellent, Level target, LocalDateTime at,
            LocalDateTime periodEnd) {
        BadgeStatus excellentStatus = statusOf(excellent);
        BadgeStatus reviewerStatus = statusOf(reviewer);
        boolean excellentActive = excellentStatus == BadgeStatus.ACTIVE;
        boolean reviewerActive = reviewerStatus == BadgeStatus.ACTIVE;

        switch (target) {
            case EXCELLENT_REVIEWER -> {
                activateOrCreate(user, excellentDef, excellent, at);
                activateOrCreate(user, reviewerDef, reviewer, at);
            }
            case REVIEWER -> {
                if (excellentActive) {
                    activateOrCreate(user, reviewerDef, reviewer, at);
                    moveToGrace(user, excellent, periodEnd, at, reviewerDef);
                } else {
                    expireGraceIfDue(excellent, periodEnd, at);
                    activateOrCreate(user, reviewerDef, reviewer, at);
                }
            }
            case NONE -> {
                if (excellentActive) {
                    activateOrCreate(user, reviewerDef, reviewer, at);
                    moveToGrace(user, excellent, periodEnd, at, reviewerDef);
                } else if (reviewerActive) {
                    moveToGrace(user, reviewer, periodEnd, at, null);
                } else {
                    expireGraceIfDue(excellent, periodEnd, at);
                    expireGraceIfDue(reviewer, periodEnd, at);
                }
            }
        }
    }

    private void applyMidCycleRecovery(User user, Badge reviewerDef, Badge excellentDef,
            UserBadge reviewer, UserBadge excellent, Level target, LocalDateTime at) {
        if (target == Level.EXCELLENT_REVIEWER) {
            recoverGraceOnly(user, excellentDef, excellent, at);
            recoverGraceOnly(user, reviewerDef, reviewer, at);
            return;
        }
        if (target == Level.REVIEWER) {
            recoverGraceOnly(user, reviewerDef, reviewer, at);
            return;
        }
    }

    private void recoverGraceOnly(User user, Badge definition, UserBadge owned, LocalDateTime at) {
        if (owned == null) {
            UserBadge award = new UserBadge(user, definition);
            award.activate(at);
            userBadges.save(award);
            return;
        }
        BadgeStatus current = statusOf(owned);
        if (current == BadgeStatus.EXPIRED) {
            return;
        }
        if (current != BadgeStatus.ACTIVE) {
            owned.activate(at);
        }
    }

    private void activateOrCreate(User user, Badge definition, UserBadge owned, LocalDateTime at) {
        if (owned == null) {
            UserBadge award = new UserBadge(user, definition);
            award.activate(at);
            userBadges.save(award);
            return;
        }
        if (statusOf(owned) != BadgeStatus.ACTIVE) {
            owned.activate(at);
        }
    }

    private void moveToGrace(User user, UserBadge owned, LocalDateTime periodEnd,
            LocalDateTime at, Badge fallbackDisplay) {
        if (owned == null || statusOf(owned) != BadgeStatus.ACTIVE) {
            return;
        }
        histories.save(new UserBadgeHistory(owned, periodEnd));
        owned.requireRenewal(ReviewerBadgePolicy.nextQuarterStart(at.toLocalDate()));
        if (user.getDisplayedBadge() != null
                && user.getDisplayedBadge().getId().equals(owned.getBadge().getId())) {
            if (fallbackDisplay != null) {
                UserBadge fallback = findOwned(user.getId(), fallbackDisplay.getId());
                if (fallback != null && statusOf(fallback) == BadgeStatus.ACTIVE) {
                    user.displayBadge(fallback.getBadge());
                } else {
                    user.displayBadge(null);
                }
            } else {
                user.displayBadge(null);
            }
        }
    }

    private void expireGraceIfDue(UserBadge owned, LocalDateTime periodEnd, LocalDateTime at) {
        if (owned == null) {
            return;
        }
        BadgeStatus current = statusOf(owned);
        boolean renewalDue = current != BadgeStatus.GRACE
                || owned.getGraceUntil() == null
                || !at.isBefore(owned.getGraceUntil());
        BadgeStatus next = ReviewerBadgePolicy.evaluate(current, false, renewalDue);
        if (next == BadgeStatus.EXPIRED && current != BadgeStatus.EXPIRED) {
            owned.expire(periodEnd);
        }
    }

    private UserBadge findOwned(UUID userId, Integer badgeId) {
        return userBadges.findByIdUserIdAndIdBadgeId(userId, badgeId).orElse(null);
    }

    private BadgeStatus statusOf(UserBadge owned) {
        if (owned == null) {
            return null;
        }
        if (owned.getStatus() != null) {
            return owned.getStatus();
        }
        return owned.isEligible() ? BadgeStatus.ACTIVE : BadgeStatus.EXPIRED;
    }

    private Badge definition(String code, String name) {
        return badges.findByCode(code).orElseThrow(() ->
                new IllegalStateException("배지 정의가 없습니다: " + name + " (" + code + ")"));
    }
}
