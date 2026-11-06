package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;
import com.newlecture.resuit.dto.SpecialJobApplicationRequest;

public interface JobApplicationService {
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
