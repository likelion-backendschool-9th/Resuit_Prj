package com.newlecture.resuit.dto.feedback;

import com.newlecture.resuit.entity.User;

// 게시글과 댓글에 함께 쓰는 작성자 정보
public record FeedbackAuthor(
    String username,
    String nickname,
    String profileImageUrl
) {
    public static FeedbackAuthor of(User user) {
        return new FeedbackAuthor(user.getUsername(), user.getNickname(), user.getProfileImageUrl());
    }
}
