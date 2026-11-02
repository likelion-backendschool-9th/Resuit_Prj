package com.newlecture.resuit.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResumeController {

	@GetMapping("/api/resumes")
	public ResponseEntity<Void> getAllResumes() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/feedback-requests")
	public ResponseEntity<Void> requestCommonResumeFeedback(
			@RequestBody(required = false) Map<String, Object> feedbackRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/feedback-requests/{requestId}")
	public ResponseEntity<Void> cancelCommonResumeFeedback(
			@PathVariable Long requestId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/common-resumes")
	public ResponseEntity<Void> getCommonResumes() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/v1/common-resumes")
	public ResponseEntity<Void> createCommonResume(
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/api/common-resumes/{c_resumeId}")
	public ResponseEntity<Void> updateCommonResume(
			@PathVariable Long c_resumeId,
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/common-resumes/{c_resumeId}")
	public ResponseEntity<Void> deleteCommonResume(@PathVariable Long c_resumeId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/common-resumes/{resumeId}")
	public ResponseEntity<Void> getCommonResume(@PathVariable Long resumeId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/target-resumes")
	public ResponseEntity<Void> getTargetResumes() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/target-resumes")
	public ResponseEntity<Void> createTargetResume(
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/api/target-resumes/{t_resumeId}")
	public ResponseEntity<Void> updateTargetResume(
			@PathVariable Long t_resumeId,
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/target-resumes/{t_resumeId}")
	public ResponseEntity<Void> deleteTargetResume(@PathVariable Long t_resumeId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/api/target-resumes/{resumeId}")
	public ResponseEntity<Void> getTargetResume(@PathVariable Long resumeId) {
		return ResponseEntity.ok().build();
	}
}
