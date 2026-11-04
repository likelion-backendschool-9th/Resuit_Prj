package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.ResumeListResponse;
import com.newlecture.resuit.service.CommonResumeService;
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

    @Override
    @Transactional(readOnly = true)
    public ResumeListResponse getMyResumes(String username) {
        return new ResumeListResponse(commonResumes.getMyResumes(username), targetResumes.getMyResumes(username));
    }
}
