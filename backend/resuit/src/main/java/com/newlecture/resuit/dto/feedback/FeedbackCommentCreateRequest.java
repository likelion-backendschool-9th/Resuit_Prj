package com.newlecture.resuit.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 피드백 댓글 작성(4.7). section: 요청한 이력서 항목(BASIC, SKILLS ...). 없으면 이력서 전체에 대한 피드백
public record FeedbackCommentCreateRequest(
    String section,
    @NotBlank(message = "피드백 내용을 입력해 주세요.")
    @Size(max = 2000, message = "피드백은 2000자까지 쓸 수 있어요.")
    String content
) {}
