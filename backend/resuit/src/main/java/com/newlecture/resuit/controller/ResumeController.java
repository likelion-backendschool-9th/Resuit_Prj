package com.newlecture.resuit.controller;

import com.newlecture.resuit.dto.AdoptFeedbackResponse;
import com.newlecture.resuit.dto.CommonResumeResponse;
import com.newlecture.resuit.dto.CreateCommonResumeRequest;
import com.newlecture.resuit.dto.UpdateCommonResumeRequest;
import com.newlecture.resuit.service.CommonResumeService;
import com.newlecture.resuit.service.CurrentUserResolver;
import com.newlecture.resuit.service.FeedbackAdoptionService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
	private final FeedbackAdoptionService adoptions;
	private final CurrentUserResolver currentUser;
	private final CommonResumeService commonResumes;

	public ResumeController(FeedbackAdoptionService adoptions, CurrentUserResolver currentUser,
			CommonResumeService commonResumes) {
		this.adoptions = adoptions;
		this.currentUser = currentUser;
		this.commonResumes = commonResumes;
	}

	@GetMapping("/")
	public ResponseEntity<Void> getAllResumes() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/feedback-requests")
	public ResponseEntity<Void> getFeedbackRequests(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) Boolean mine) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/feedback-requests")
	public ResponseEntity<Void> createFeedbackRequest(
			@RequestBody(required = false) Map<String, Object> feedbackRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/feedback-requests/{requestId}")
	public ResponseEntity<Void> cancelCommonResumeFeedback(
			@PathVariable Long requestId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/resumes/{id}")
	public ResponseEntity<Void> previewResume(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/feedback-requests/{id}/comments")
	public ResponseEntity<Void> getFeedbackComments(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/feedback-requests/{id}/comments")
	public ResponseEntity<Void> createFeedbackComment(
			@PathVariable Long id,
			@RequestBody(required = false) Map<String, Object> commentRequest) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/feedbacks/{feedbackId}/adopt")
	public AdoptFeedbackResponse adoptFeedback(@PathVariable Long feedbackId,
			@RequestHeader(value = "X-Username", required = false) String username) {
		return adoptions.adoptByUsername(feedbackId, username);
	}

	@GetMapping("/common-resumes")
	public List<CommonResumeResponse> getCommonResumes(
			@RequestHeader(value = "X-Username", required = false) String username) {
		return commonResumes.listMine(currentUser.requireUserId(username));
	}

	@PostMapping("/common-resumes")
	@ResponseStatus(HttpStatus.CREATED)
	public CommonResumeResponse createCommonResume(
			@RequestHeader(value = "X-Username", required = false) String username,
			@RequestBody CreateCommonResumeRequest request) {
		return commonResumes.create(currentUser.requireUserId(username), request);
	}

	@PatchMapping("/common-resumes/{c_resumeId}")
	public CommonResumeResponse updateCommonResume(
			@RequestHeader(value = "X-Username", required = false) String username,
			@PathVariable("c_resumeId") Long resumeId,
			@RequestBody UpdateCommonResumeRequest request) {
		return commonResumes.update(currentUser.requireUserId(username), resumeId, request);
	}

	@DeleteMapping("/common-resumes/{c_resumeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCommonResume(
			@RequestHeader(value = "X-Username", required = false) String username,
			@PathVariable("c_resumeId") Long resumeId) {
		commonResumes.delete(currentUser.requireUserId(username), resumeId);
	}

	@GetMapping("/common-resumes/{resumeId}")
	public CommonResumeResponse getCommonResume(
			@RequestHeader(value = "X-Username", required = false) String username,
			@PathVariable Long resumeId) {
		return commonResumes.getMine(currentUser.requireUserId(username), resumeId);
	}

	@GetMapping("/target-resumes")
	public ResponseEntity<Void> getTargetResumes() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/target-resumes")
	public ResponseEntity<Void> createTargetResume(
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/target-resumes/{t_resumeId}")
	public ResponseEntity<Void> updateTargetResume(
			@PathVariable Long t_resumeId,
			@RequestBody(required = false) Map<String, Object> resumeRequest) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/target-resumes/{t_resumeId}")
	public ResponseEntity<Void> deleteTargetResume(@PathVariable Long t_resumeId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/target-resumes/{resumeId}")
	public ResponseEntity<Void> getTargetResume(@PathVariable Long resumeId) {
		return ResponseEntity.ok().build();
	}
}
