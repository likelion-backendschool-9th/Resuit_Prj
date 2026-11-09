package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.BadgeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProfileResponse(
        UUID id, String username, String nickname, String profileImageUrl,
        String selfIntroduction, Map<String, Object> widgets,
        long followers, long following, long posts, long feedbacks,
        boolean isFollowing, ResumeSummary pinnedResume,
        BadgeSummary displayedBadge, List<BadgeSummary> badges,
        List<BadgeHistorySummary> badgeHistory, List<CareerResponse> careers,
        boolean companyVerified, LocalDateTime companyVerifiedAt) {

    public record ResumeSummary(Long id, String title, String field) {
    }

    public record BadgeSummary(Integer id, String code, String name, String iconUrl,
            BadgeStatus status, LocalDateTime acquiredAt, LocalDateTime graceUntil,
            LocalDateTime expiresAt, LocalDateTime continuousSince, int streakQuarters) {
    }

    public record BadgeHistorySummary(Integer badgeId, String badgeName,
            LocalDateTime activeFrom, LocalDateTime activeUntil) {
    }
}
