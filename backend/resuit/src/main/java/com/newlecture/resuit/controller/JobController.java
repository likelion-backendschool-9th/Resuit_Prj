package com.newlecture.resuit.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {

	// 정해진 dto 규격이 없지만 일단 작성해놓음. 참고
	@GetMapping("/api/jobs")
	public ResponseEntity<Void> getJobs(
			@RequestParam(required = false) String type,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/jobs/{id}")
	public ResponseEntity<Void> getJob(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/jobs/{jobId}/applications")
	public ResponseEntity<Void> applyForJob(
			@PathVariable Long jobId,
			@RequestParam(required = false) String mode,
			@RequestBody(required = false) Map<String, Object> applicationRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/jobs/{jobId}/applications")
	public ResponseEntity<Void> cancelApplication(@PathVariable Long jobId) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/jobs")
	public ResponseEntity<Void> createJob(
			@RequestBody(required = false) Map<String, Object> jobRequest) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/api/jobs/{jobId}")
	public ResponseEntity<Void> updateJob(
			@PathVariable Long jobId,
			@RequestBody(required = false) Map<String, Object> jobRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/jobs/{jobId}")
	public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
		return ResponseEntity.ok().build();
	}
}
