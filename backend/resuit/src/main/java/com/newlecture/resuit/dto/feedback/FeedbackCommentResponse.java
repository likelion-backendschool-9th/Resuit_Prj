package com.newlecture.resuit.dto.feedback;

import com.newlecture.resuit.entity.FeedbackComment;
import java.time.LocalDateTime;

// 피드백 댓글(4.6, 4.7). section이 null이면 이력서 전체에 대한 피드백
// reviewerBadge: 작성자가 프로필에 내건 배지 이름. 없으면 null
public record FeedbackCommentResponse(
    Long commentId,
    Long parentId,
    FeedbackAuthor author,
    String reviewerBadge,
    String section,
    String content,
    boolean adopted,
    LocalDateTime adoptedAt,
    boolean mine,
    LocalDateTime createdAt
) {
    public static FeedbackCommentResponse of(FeedbackComment comment, boolean mine) {
        var user = comment.getUser();
        var badge = user.getDisplayedBadge();
        return new FeedbackCommentResponse(comment.getCommentId(),
            comment.getParent() == null ? null : comment.getParent().getCommentId(), FeedbackAuthor.of(user),
            badge == null ? null : badge.getName(), comment.getSection(), comment.getContent(),
            comment.isAdopted(), comment.getAdoptedAt(), mine, comment.getCreatedAt());
    }
}
