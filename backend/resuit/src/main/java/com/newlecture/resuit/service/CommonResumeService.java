package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.dto.resume.CommonResumeRequest;
import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.dto.resume.CommonResumeUpdateCommand;
import com.newlecture.resuit.dto.resume.RestoredResumeResponse;
import com.newlecture.resuit.entity.JobApplication;
import java.util.List;

public interface CommonResumeService {
    RestoredResumeResponse restore(JobApplication application);
    List<CommonResumeSummary> getMyResumes(String username);
    CommonResumeDetailResponse create(String username, CommonResumeRequest request);
    CommonResumeDetailResponse update(CommonResumeUpdateCommand command);
    void delete(Long resumeId, String username);
    CommonResumeDetailResponse getMyResume(Long resumeId, String username);

    public static class UnauthenticatedUserException extends RuntimeException {}
    public static class ResumeNotFoundException extends RuntimeException {}
    public static class ResumeForbiddenException extends RuntimeException {}
    public static class ResumeInUseException extends RuntimeException {
        public ResumeInUseException() {}
        public ResumeInUseException(String message) { super(message); }
    }

    public static class InvalidResumeException extends RuntimeException {
        public InvalidResumeException(String message) {
            super(message);
        }
    }
}
