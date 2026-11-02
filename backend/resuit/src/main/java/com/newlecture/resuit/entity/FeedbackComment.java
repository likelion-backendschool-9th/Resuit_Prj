package com.newlecture.resuit.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 피드백 댓글 및 대댓글 (feedback_comments). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackComment {
    private Long commentId;
    private Long parentId;
    private Long feedbackId;
    private UUID userId;
    private String section;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
