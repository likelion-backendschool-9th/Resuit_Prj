package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.ResumeListResponse;

public interface ResumeService {
    ResumeListResponse getMyResumes(String username);
}
