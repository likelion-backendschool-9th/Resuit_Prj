package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record AdoptFeedbackResponse(Long commentId, boolean adopted,
        LocalDateTime adoptedAt) {
}
