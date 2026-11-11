package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanyApplicantsResponse;
import com.newlecture.resuit.dto.CompanyApplicationDetailResponse;
import com.newlecture.resuit.dto.CompanyApplicationQuery;
import com.newlecture.resuit.dto.CompanyApplicationsQuery;
import com.newlecture.resuit.dto.JobApplicationCommand;
import com.newlecture.resuit.dto.JobApplicationCommonResponse;
import com.newlecture.resuit.dto.JobApplicationTargetResponse;
import com.newlecture.resuit.dto.MyJobApplicationResponse;
import com.newlecture.resuit.dto.resume.SubmittedPhotoResponse;
import com.newlecture.resuit.dto.resume.SubmittedResumeResponse;
import com.newlecture.resuit.dto.resume.SubmittedResumeSummary;
import com.newlecture.resuit.entity.JobApplication;
import java.util.List;

public interface JobApplicationService {
    CompanyApplicantsResponse getCompanyApplicants(CompanyApplicationsQuery query);
    CompanyApplicationDetailResponse getCompanyApplication(CompanyApplicationQuery query);
    SubmittedPhotoResponse getCompanyApplicationPhoto(CompanyApplicationQuery query);
    JobApplicationCommonResponse applyForGeneralJob(JobApplicationCommand command);
    JobApplicationTargetResponse applyForSpecialJob(JobApplicationCommand command);
    MyJobApplicationResponse getMyApplication(Long jobId, String username);
    List<SubmittedResumeSummary> getSubmittedResumes(String username);
    JobApplication getRestorableApplication(Long id, String username);
    SubmittedResumeResponse getSubmittedResume(Long id, String username);
    SubmittedPhotoResponse getSubmittedPhoto(Long id, String username);
    void purgeExpiredSnapshot(Long id);
    void cancelApplication(Long jobId, String username);
}
