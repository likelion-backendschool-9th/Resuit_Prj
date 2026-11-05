package com.newlecture.resuit.dto.resume;

// mode: BASE(공통 이력서 내용을 복사해 시작) | BLANK(빈 이력서로 시작). title이 없으면 공고 제목을 쓴다.
public record TargetResumeCreateRequest(Long jobId, String mode, Long commonResumeId, String title) {}
