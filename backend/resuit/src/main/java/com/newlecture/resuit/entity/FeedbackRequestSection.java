package com.newlecture.resuit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 피드백 요청의 희망 분야 (feedback_request_sections). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestSection {
    private Long sectionId;
    private Long requestId;
    private String sectionType;
}
