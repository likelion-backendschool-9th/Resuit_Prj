package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.CommonResume;
import java.time.LocalDateTime;
import java.util.Map;

public record CommonResumeDetailResponse(
    Long id,
    String title,
    String photoUrl,
    String field,
    boolean isPublic,
    Map<String, Object> content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommonResumeDetailResponse from(CommonResume resume) {
        return new CommonResumeDetailResponse(resume.getId(), resume.getTitle(), resume.getPhotoUrl(), resume.getField(),
            resume.isPublic(), resume.getContent(), resume.getCreatedAt(), resume.getUpdatedAt());
    }
}
