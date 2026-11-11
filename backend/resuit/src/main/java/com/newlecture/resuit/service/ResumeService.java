package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.RestoredResumeResponse;
import com.newlecture.resuit.dto.resume.ResumeListResponse;

public interface ResumeService {
    RestoredResumeResponse restoreSubmission(Long applicationId, String username);
    ResumeListResponse getMyResumes(String username);
}
