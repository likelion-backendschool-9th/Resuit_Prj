package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.JobResponse;

public interface JobService {
	Page<JobResponse> getJobs(String type, Pageable pageable);
}
