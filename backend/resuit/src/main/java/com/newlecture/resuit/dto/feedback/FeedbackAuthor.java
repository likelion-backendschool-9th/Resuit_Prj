package com.newlecture.resuit.dto.feedback;

import com.newlecture.resuit.entity.User;

// 게시글과 댓글에 함께 쓰는 작성자 정보
public record FeedbackAuthor(
    String username,
    String nickname,
    String profileImageUrl,
    String displayedBadgeName
) {
    public static FeedbackAuthor of(User user) {
        var badge = user.getDisplayedBadge();
        return new FeedbackAuthor(
            user.getUsername(),
            user.getNickname(),
            user.getProfileImageUrl(),
            badge == null ? null : badge.getName());
    }
}
