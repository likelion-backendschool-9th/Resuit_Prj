package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.dto.resume.CommonResumeRequest;
import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.dto.resume.CommonResumeUpdateCommand;
import java.util.List;

public interface CommonResumeService {
    List<CommonResumeSummary> getMyResumes(String username);
    CommonResumeDetailResponse create(String username, CommonResumeRequest request);
    CommonResumeDetailResponse update(CommonResumeUpdateCommand command);

    public static class UnauthenticatedUserException extends RuntimeException {}
    public static class ResumeNotFoundException extends RuntimeException {}
    public static class ResumeForbiddenException extends RuntimeException {}
    public static class ResumeInUseException extends RuntimeException {}

    public static class InvalidResumeException extends RuntimeException {
        public InvalidResumeException(String message) {
            super(message);
        }
    }
}
