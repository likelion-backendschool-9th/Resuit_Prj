package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record CommonResumeResponse(
        Long id,
        String title,
        String field,
        boolean isPublic,
        Map<String, Object> content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
