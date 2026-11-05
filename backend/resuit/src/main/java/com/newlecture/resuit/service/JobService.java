package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.CreateJobRequest;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;
import com.newlecture.resuit.dto.SpecialJobApplicationRequest;

public interface JobService {
	JobDetailResponse createJob(CreateJobRequest request, String username);
	JobDetailResponse updateJob(Long jobId, CreateJobRequest request, String username);
	Page<JobResponse> getJobs(String type, Pageable pageable);
	JobDetailResponse getJob(Long id);
	JobApplicationResponse applyForGeneralJob(
			Long jobId,
			GeneralJobApplicationRequest request,
			String username);

	JobApplicationResponse applyForSpecialJob(
			Long jobId,
			SpecialJobApplicationRequest request,
			String username);
	void cancelApplication(Long jobId, String username);
}
