package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.JobApplicationCommand;
import com.newlecture.resuit.dto.JobApplicationCommonResponse;
import com.newlecture.resuit.dto.JobApplicationTargetResponse;
import com.newlecture.resuit.dto.MyJobApplicationResponse;

public interface JobApplicationService {
    JobApplicationCommonResponse applyForGeneralJob(JobApplicationCommand command);
    JobApplicationTargetResponse applyForSpecialJob(JobApplicationCommand command);
    MyJobApplicationResponse getMyApplication(Long jobId, String username);
    void cancelApplication(Long jobId, String username);
}
