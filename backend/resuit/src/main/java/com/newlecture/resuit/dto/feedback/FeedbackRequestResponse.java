package com.newlecture.resuit.dto.feedback;

import com.newlecture.resuit.entity.FeedbackRequest;
import java.time.LocalDateTime;
import java.util.List;

public record FeedbackRequestResponse(
    Long id,
    Long commonResumeId,
    Long targetResumeId,
    String content,
    String category,
    List<String> sections,
    LocalDateTime createdAt
) {
    public static FeedbackRequestResponse of(FeedbackRequest request, List<String> sections) {
        var common = request.getCommonResume();
        var target = request.getTargetResume();
        return new FeedbackRequestResponse(request.getId(), common == null ? null : common.getId(),
            target == null ? null : target.getId(), request.getContent(), request.getCategory(), sections,
            request.getCreatedAt());
    }
}
