package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.TargetResumeCreateRequest;
import com.newlecture.resuit.dto.resume.TargetResumeDetailResponse;
import com.newlecture.resuit.dto.resume.TargetResumeSummary;
import com.newlecture.resuit.dto.resume.TargetResumeUpdateCommand;
import java.util.List;

// 로그인·이력서 없음·권한·사용 중·잘못된 값 오류는 CommonResumeService의 예외를 함께 쓴다.
public interface TargetResumeService {
    List<TargetResumeSummary> getMyResumes(String username);
    TargetResumeDetailResponse create(String username, TargetResumeCreateRequest request);
    TargetResumeDetailResponse update(TargetResumeUpdateCommand command);
    void delete(Long resumeId, String username);

    public static class JobNotFoundException extends RuntimeException {}
    public static class NotSpecialJobException extends RuntimeException {}
    public static class TargetResumeExistsException extends RuntimeException {}
}
