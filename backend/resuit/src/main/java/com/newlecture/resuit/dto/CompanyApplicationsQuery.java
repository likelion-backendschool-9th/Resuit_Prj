package com.newlecture.resuit.dto;

import org.springframework.data.domain.Pageable;

public record CompanyApplicationsQuery(Long jobId, String username, Pageable pageable) {}
