package com.newlecture.resuit.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.service.JobService;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
	private final JobService jobService;

	public JobController(JobService jobService) {
		this.jobService = jobService;
	}

	@GetMapping
	public Page<JobResponse> getJobs(
			@RequestParam(required = false) String type,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return jobService.getJobs(type, PageRequest.of(page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Void> getJob(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{jobId}/applications")
	public ResponseEntity<Void> applyForJob(
			@PathVariable Long jobId,
			@RequestParam(required = false) String mode,
			@RequestBody(required = false) Map<String, Object> applicationRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{jobId}/applications")
	public ResponseEntity<Void> cancelApplication(@PathVariable Long jobId) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/")
	public ResponseEntity<Void> createJob(
			@RequestBody(required = false) Map<String, Object> jobRequest) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{jobId}")
	public ResponseEntity<Void> updateJob(
			@PathVariable Long jobId,
			@RequestBody(required = false) Map<String, Object> jobRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{jobId}")
	public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
		return ResponseEntity.ok().build();
	}
}
