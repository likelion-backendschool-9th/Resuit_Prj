package com.newlecture.resuit.dto;

import org.springframework.data.domain.Page;

public record CompanyApplicantsResponse(PostedJobSummary job, Page<CompanyApplicantSummary> applications) {}
