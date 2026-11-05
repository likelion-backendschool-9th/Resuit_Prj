package com.newlecture.resuit.dto.resume;

public record CommonResumeUpdateCommand(Long resumeId, String username, CommonResumeRequest request) {}
