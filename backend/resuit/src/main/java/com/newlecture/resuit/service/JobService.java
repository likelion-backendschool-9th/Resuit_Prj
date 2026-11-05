package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.CreateJobRequest;
import com.newlecture.resuit.dto.JobDetailResponse;

public interface JobService {
	JobDetailResponse createJob(CreateJobRequest request, String username);
	JobDetailResponse updateJob(Long jobId, CreateJobRequest request, String username);
	Page<JobResponse> getJobs(String type, Pageable pageable);
	JobDetailResponse getJob(Long id);
}
