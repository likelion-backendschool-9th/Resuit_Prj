package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.dto.resume.CommonResumeRequest;
import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.dto.resume.CommonResumeUpdateCommand;
import com.newlecture.resuit.service.CommonResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ResumeController {

	private final CommonResumeService commonResumeService;

	@GetMapping("/resumes")
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
	public ResponseEntity<Void> adoptFeedback(@PathVariable Long feedbackId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/common-resumes")
	public ResponseEntity<List<CommonResumeSummary>> getCommonResumes(Principal principal) {
		return ResponseEntity.ok(commonResumeService.getMyResumes(username(principal)));
	}

	@PostMapping("/common-resumes")
	public ResponseEntity<CommonResumeDetailResponse> createCommonResume(
			@RequestBody(required = false) CommonResumeRequest resumeRequest, Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(commonResumeService.create(username(principal), resumeRequest));
	}

	@PatchMapping("/common-resumes/{c_resumeId}")
	public ResponseEntity<CommonResumeDetailResponse> updateCommonResume(
			@PathVariable Long c_resumeId,
			@RequestBody(required = false) CommonResumeRequest resumeRequest, Principal principal) {
		return ResponseEntity.ok(commonResumeService.update(
			new CommonResumeUpdateCommand(c_resumeId, username(principal), resumeRequest)));
	}

	@DeleteMapping("/common-resumes/{c_resumeId}")
	public ResponseEntity<Void> deleteCommonResume(@PathVariable Long c_resumeId, Principal principal) {
		commonResumeService.delete(c_resumeId, username(principal));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/common-resumes/{resumeId}")
	public ResponseEntity<Void> getCommonResume(@PathVariable Long resumeId) {
		return ResponseEntity.ok().build();
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

	private static String username(Principal principal) {
		return principal == null ? null : principal.getName();
	}

	public record ResumeErrorResponse(String code, String message) {}

	@ExceptionHandler(CommonResumeService.UnauthenticatedUserException.class)
	public ResponseEntity<ResumeErrorResponse> unauthenticated() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ResumeErrorResponse("UNAUTHENTICATED", "로그인이 필요합니다."));
	}

	@ExceptionHandler(CommonResumeService.ResumeNotFoundException.class)
	public ResponseEntity<ResumeErrorResponse> resumeNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ResumeErrorResponse("RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."));
	}

	@ExceptionHandler(CommonResumeService.ResumeForbiddenException.class)
	public ResponseEntity<ResumeErrorResponse> resumeForbidden() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(new ResumeErrorResponse("RESUME_FORBIDDEN", "본인 이력서만 다룰 수 있습니다."));
	}

	@ExceptionHandler(CommonResumeService.ResumeInUseException.class)
	public ResponseEntity<ResumeErrorResponse> resumeInUse() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ResumeErrorResponse("RESUME_IN_USE",
				"지원, 피드백 요청, 특화 이력서, 대표 이력서에 쓰고 있는 이력서는 삭제할 수 없습니다."));
	}

	@ExceptionHandler(CommonResumeService.InvalidResumeException.class)
	public ResponseEntity<ResumeErrorResponse> invalidResume(CommonResumeService.InvalidResumeException e) {
		return ResponseEntity.badRequest().body(new ResumeErrorResponse("INVALID_RESUME", e.getMessage()));
	}
}
