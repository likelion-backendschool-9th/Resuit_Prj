package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;

public interface JobService {
	Page<JobResponse> getJobs(String type, Pageable pageable);
	JobDetailResponse getJob(Long id);
	JobApplicationResponse applyForGeneralJob(
			Long jobId,
			GeneralJobApplicationRequest request,
			String username);
}
