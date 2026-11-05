package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record ProfileFeedbackResponse(Long id, Long feedbackRequestId, String section,
        String content, boolean adopted, LocalDateTime createdAt) {
}
