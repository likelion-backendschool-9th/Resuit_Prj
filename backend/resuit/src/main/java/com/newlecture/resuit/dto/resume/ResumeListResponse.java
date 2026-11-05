package com.newlecture.resuit.dto.resume;

import java.util.List;

// 이력서 탭(전체·공통·특화)에 필요한 목록을 한 번에 돌려준다.
public record ResumeListResponse(List<CommonResumeSummary> commonResumes, List<TargetResumeSummary> targetResumes) {}
