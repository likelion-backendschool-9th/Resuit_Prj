package com.newlecture.resuit.dto;

// 일반·특화 지원 서비스가 공통으로 사용하는 내부 입력. username은 인증된 Principal에서만 받는다.
public record JobApplicationCommand(Long jobId, Long resumeId, String username) {}
