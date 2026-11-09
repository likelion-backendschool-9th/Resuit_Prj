package com.newlecture.resuit.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.CreateJobRequest;
import com.newlecture.resuit.dto.SaveJobDraftRequest;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationCommand;
import com.newlecture.resuit.dto.JobApplicationCommonResponse;
import com.newlecture.resuit.dto.JobApplicationTargetResponse;
import com.newlecture.resuit.dto.MyJobApplicationResponse;
import com.newlecture.resuit.dto.JobErrorResponse;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.newlecture.resuit.dto.SpecialJobApplicationRequest;
import com.newlecture.resuit.service.JobService;
import com.newlecture.resuit.service.JobApplicationService;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
	private final JobService jobService;
	private final JobApplicationService jobApplicationService;

	public JobController(JobService jobService, JobApplicationService jobApplicationService) {
		this.jobService = jobService;
		this.jobApplicationService = jobApplicationService;
	}

    @PostMapping("/me/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public JobDetailResponse createDraft(@Valid @RequestBody SaveJobDraftRequest request, Principal principal) {
        return jobService.createDraft(request, principal.getName());
    }

    @GetMapping("/me/drafts")
    public Page<JobResponse> getDrafts(Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobService.getDrafts(principal.getName(), PageRequest.of(page, size));
    }

    @GetMapping("/me/drafts/{jobId}")
    public JobDetailResponse getDraft(@PathVariable Long jobId, Principal principal) {
        return jobService.getDraft(jobId, principal.getName());
    }

    @PutMapping("/me/drafts/{jobId}")
    public JobDetailResponse updateDraft(@PathVariable Long jobId,
            @Valid @RequestBody SaveJobDraftRequest request, Principal principal) {
        return jobService.updateDraft(jobId, request, principal.getName());
    }

	@GetMapping
	public Page<JobResponse> getJobs(
			@RequestParam(required = false) String type,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return jobService.getJobs(type, PageRequest.of(page, size));
	}

	@GetMapping("/{id}")
	public JobDetailResponse getJob(@PathVariable Long id) {
		return jobService.getJob(id);
	}

    @GetMapping("/{jobId}/applications/me")
    public ResponseEntity<MyJobApplicationResponse> getMyApplication(@PathVariable Long jobId, Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(jobApplicationService.getMyApplication(jobId, principal.getName()));
    }

	@PostMapping("/{jobId}/applications")
	public JobApplicationCommonResponse applyForJob(
			@PathVariable Long jobId,
			@Valid @RequestBody GeneralJobApplicationRequest applicationRequest,
			Principal principal) {
		return jobApplicationService.applyForGeneralJob(new JobApplicationCommand(jobId, applicationRequest.commonResumeId(), principal.getName()));
	}

	@PostMapping("/{jobId}/special-applications")
	public JobApplicationTargetResponse applyForSpecialJob(
			@PathVariable Long jobId,
			@Valid @RequestBody SpecialJobApplicationRequest applicationRequest,
			Principal principal) {
		return jobApplicationService.applyForSpecialJob(new JobApplicationCommand(jobId, applicationRequest.targetResumeId(), principal.getName()));
	}

	@DeleteMapping("/{jobId}/applications")
	public ResponseEntity<Void> cancelApplication(
			@PathVariable Long jobId,
			Principal principal) {
		jobApplicationService.cancelApplication(jobId, principal.getName());
		return ResponseEntity.noContent().build();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public JobDetailResponse createJob(
			@Valid @RequestBody CreateJobRequest request,
			Principal principal) {
		return jobService.createJob(request, principal.getName());
	}

	@PatchMapping("/{jobId}")
	public ResponseEntity<JobDetailResponse> updateJob(
			@PathVariable Long jobId,
			@Valid @RequestBody CreateJobRequest request,
			Principal principal) {
		return ResponseEntity.ok(jobService.updateJob(jobId, request, principal.getName()));
	}

	@DeleteMapping("/{jobId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteJob(@PathVariable Long jobId, Principal principal) {
		jobService.deleteJob(jobId, principal.getName());
	}
    // 공고 컨트롤러에만 적용하여 다른 도메인의 예외 응답 계약은 유지한다.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JobErrorResponse invalidRequest(IllegalArgumentException exception) {
        return new JobErrorResponse("INVALID_JOB_REQUEST", exception.getMessage());
    }
}
