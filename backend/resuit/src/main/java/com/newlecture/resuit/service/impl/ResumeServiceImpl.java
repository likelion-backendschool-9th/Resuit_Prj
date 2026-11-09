package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.RestoredResumeResponse;
import com.newlecture.resuit.dto.resume.ResumeListResponse;
import com.newlecture.resuit.service.CommonResumeService;
import com.newlecture.resuit.service.JobApplicationService;
import com.newlecture.resuit.service.ResumeService;
import com.newlecture.resuit.service.TargetResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {
    private final CommonResumeService commonResumes;
    private final TargetResumeService targetResumes;
    private final JobApplicationService applications;

    @Override
    @Transactional
    public RestoredResumeResponse restoreSubmission(Long applicationId, String username) {
        var application = applications.getRestorableApplication(applicationId, username);
        return "COMMON".equals(application.getSubmittedResumeType())
            ? commonResumes.restore(application) : targetResumes.restore(application);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeListResponse getMyResumes(String username) {
        return new ResumeListResponse(commonResumes.getMyResumes(username), targetResumes.getMyResumes(username), applications.getSubmittedResumes(username));
    }
}
