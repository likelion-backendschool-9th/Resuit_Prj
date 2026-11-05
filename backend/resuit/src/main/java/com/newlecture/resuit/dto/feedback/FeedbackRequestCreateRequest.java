package com.newlecture.resuit.dto.feedback;

import java.util.List;

// 피드백 요청(3.2, 4.2). 공통 이력서와 특화 이력서 중 정확히 하나를 고른다.
// category가 없으면 이력서의 직무 분야를 쓴다. sections: 봐 줬으면 하는 이력서 항목
public record FeedbackRequestCreateRequest(
    Long commonResumeId,
    Long targetResumeId,
    String content,
    String category,
    List<String> sections
) {}
