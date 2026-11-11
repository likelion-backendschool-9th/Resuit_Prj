package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CreateJobRequest;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.PostedJobSummary;
import com.newlecture.resuit.dto.SaveJobDraftRequest;
import com.newlecture.resuit.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    Page<PostedJobSummary> getPostedJobs(String username, Pageable pageable);
    Job requireOwnedPostedJob(Long jobId, String username);
    Page<JobResponse> getMyJobs(String username, Pageable pageable);

    JobDetailResponse createDraft(SaveJobDraftRequest request, String username);
    JobDetailResponse updateDraft(Long jobId, SaveJobDraftRequest request, String username);
    Page<JobResponse> getDrafts(String username, Pageable pageable);
    JobDetailResponse getDraft(Long jobId, String username);
	void deleteJob(Long jobId, String username);
	JobDetailResponse createJob(CreateJobRequest request, String username);
	JobDetailResponse updateJob(Long jobId, CreateJobRequest request, String username);
	Page<JobResponse> getJobs(String type, Pageable pageable);
	JobDetailResponse getJob(Long id);
}
