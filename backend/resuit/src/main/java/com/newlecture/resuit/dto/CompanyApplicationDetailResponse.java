package com.newlecture.resuit.dto;

import com.newlecture.resuit.dto.resume.SubmittedResumeResponse;

public record CompanyApplicationDetailResponse(PostedJobSummary job, CompanyApplicantSummary application,
        SubmittedResumeResponse resume) {}
