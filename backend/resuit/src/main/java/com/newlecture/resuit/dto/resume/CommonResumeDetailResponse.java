package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.CommonResume;
import java.time.LocalDateTime;
import java.util.Map;

public record CommonResumeDetailResponse(
    Long id,
    String title,
    String field,
    boolean isPublic,
    Map<String, Object> content,
    String photoUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommonResumeDetailResponse from(CommonResume resume) {
        return new CommonResumeDetailResponse(resume.getId(), resume.getTitle(), resume.getField(),
            resume.isPublic(), resume.getContent(), resume.getPhotoUrl(), resume.getCreatedAt(), resume.getUpdatedAt());
    }
}
