package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.FeedbackComment;
import com.newlecture.resuit.entity.FeedbackRequest;
import java.time.LocalDateTime;

public record ProfileFeedbackResponse(Long id, Long feedbackRequestId, String resumeTitle,
        String section, String content, boolean adopted, LocalDateTime createdAt) {

    public static ProfileFeedbackResponse of(FeedbackComment comment) {
        FeedbackRequest request = comment.getFeedbackRequest();
        return new ProfileFeedbackResponse(comment.getCommentId(), request.getId(),
                resumeTitleOf(request), comment.getSection(), comment.getContent(),
                comment.isAdopted(), comment.getCreatedAt());
    }

    private static String resumeTitleOf(FeedbackRequest request) {
        if (request.getCommonResume() != null) {
            return request.getCommonResume().getTitle();
        }
        if (request.getTargetResume() != null) {
            return request.getTargetResume().getTitle();
        }
        return null;
    }
}
