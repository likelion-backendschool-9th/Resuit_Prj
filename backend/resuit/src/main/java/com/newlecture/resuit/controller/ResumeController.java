package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.newlecture.resuit.dto.feedback.FeedbackRequestCreateRequest;
import com.newlecture.resuit.dto.feedback.FeedbackRequestResponse;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.dto.resume.CommonResumeRequest;
import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.dto.resume.CommonResumeUpdateCommand;
import com.newlecture.resuit.dto.resume.ResumeListResponse;
import com.newlecture.resuit.dto.resume.ResumePreviewCondition;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import com.newlecture.resuit.dto.resume.TargetResumeCreateRequest;
import com.newlecture.resuit.dto.resume.TargetResumeDetailResponse;
import com.newlecture.resuit.dto.resume.TargetResumeSummary;
import com.newlecture.resuit.dto.resume.TargetResumeUpdateCommand;
import com.newlecture.resuit.dto.resume.TargetResumeUpdateRequest;
import com.newlecture.resuit.dto.AdoptFeedbackResponse;
import com.newlecture.resuit.service.CommonResumeService;
import com.newlecture.resuit.service.FeedbackAdoptionService;
import com.newlecture.resuit.service.FeedbackBoardService;
import com.newlecture.resuit.service.FeedbackRequestService;
import com.newlecture.resuit.service.ResumeService;
import com.newlecture.resuit.service.TargetResumeService;
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

	private final ResumeService resumeService;
	private final FeedbackRequestService feedbackRequestService;
	private final CommonResumeService commonResumeService;
	private final TargetResumeService targetResumeService;
	private final FeedbackAdoptionService feedbackAdoptionService;
	private final FeedbackBoardService feedbackBoardService;

	@GetMapping("/resumes")
	public ResponseEntity<ResumeListResponse> getAllResumes(Principal principal) {
		return ResponseEntity.ok(resumeService.getMyResumes(username(principal)));
	}

	@GetMapping("/feedback-requests")
	public ResponseEntity<List<FeedbackRequestSummary>> getFeedbackRequests(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) Boolean mine, Principal principal) {
		return ResponseEntity.ok(feedbackBoardService.getRequests(username(principal),
			new FeedbackRequestSearchCondition(category, Boolean.TRUE.equals(mine))));
	}

	@PostMapping("/feedback-requests")
	public ResponseEntity<FeedbackRequestResponse> createFeedbackRequest(
			@RequestBody(required = false) FeedbackRequestCreateRequest feedbackRequest, Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(feedbackRequestService.create(username(principal), feedbackRequest));
	}

	@DeleteMapping("/feedback-requests/{requestId}")
	public ResponseEntity<Void> cancelCommonResumeFeedback(
			@PathVariable Long requestId, Principal principal) {
		feedbackRequestService.cancel(requestId, username(principal));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/resumes/{id}")
	public ResponseEntity<ResumePreviewResponse> previewResume(
			@PathVariable Long id, @RequestParam(required = false) String type, Principal principal) {
		return ResponseEntity.ok(feedbackBoardService.getResumePreview(username(principal),
			new ResumePreviewCondition(id, type)));
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
	public ResponseEntity<AdoptFeedbackResponse> adoptFeedback(
			@PathVariable Long feedbackId, Principal principal) {
		return ResponseEntity.ok(feedbackAdoptionService.adoptByUsername(feedbackId, username(principal)));
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
	public ResponseEntity<CommonResumeDetailResponse> getCommonResume(@PathVariable Long resumeId, Principal principal) {
		return ResponseEntity.ok(commonResumeService.getMyResume(resumeId, username(principal)));
	}

	@GetMapping("/target-resumes")
	public ResponseEntity<List<TargetResumeSummary>> getTargetResumes(Principal principal) {
		return ResponseEntity.ok(targetResumeService.getMyResumes(username(principal)));
	}

	@PostMapping("/target-resumes")
	public ResponseEntity<TargetResumeDetailResponse> createTargetResume(
			@RequestBody(required = false) TargetResumeCreateRequest resumeRequest, Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(targetResumeService.create(username(principal), resumeRequest));
	}

	@PatchMapping("/target-resumes/{t_resumeId}")
	public ResponseEntity<TargetResumeDetailResponse> updateTargetResume(
			@PathVariable Long t_resumeId,
			@RequestBody(required = false) TargetResumeUpdateRequest resumeRequest, Principal principal) {
		return ResponseEntity.ok(targetResumeService.update(
			new TargetResumeUpdateCommand(t_resumeId, username(principal), resumeRequest)));
	}

	@DeleteMapping("/target-resumes/{t_resumeId}")
	public ResponseEntity<Void> deleteTargetResume(@PathVariable Long t_resumeId, Principal principal) {
		targetResumeService.delete(t_resumeId, username(principal));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/target-resumes/{resumeId}")
	public ResponseEntity<TargetResumeDetailResponse> getTargetResume(@PathVariable Long resumeId, Principal principal) {
		return ResponseEntity.ok(targetResumeService.getMyResume(resumeId, username(principal)));
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

	@ExceptionHandler(TargetResumeService.JobNotFoundException.class)
	public ResponseEntity<ResumeErrorResponse> jobNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ResumeErrorResponse("JOB_NOT_FOUND", "공고를 찾을 수 없습니다."));
	}

	@ExceptionHandler(TargetResumeService.NotSpecialJobException.class)
	public ResponseEntity<ResumeErrorResponse> notSpecialJob() {
		return ResponseEntity.badRequest()
			.body(new ResumeErrorResponse("NOT_SPECIAL_JOB", "특화 공고에만 특화 이력서를 만들 수 있습니다."));
	}

	@ExceptionHandler(FeedbackRequestService.FeedbackRequestNotFoundException.class)
	public ResponseEntity<ResumeErrorResponse> feedbackRequestNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ResumeErrorResponse("FEEDBACK_REQUEST_NOT_FOUND", "피드백 요청을 찾을 수 없습니다."));
	}

	@ExceptionHandler(FeedbackRequestService.FeedbackRequestForbiddenException.class)
	public ResponseEntity<ResumeErrorResponse> feedbackRequestForbidden() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(new ResumeErrorResponse("FEEDBACK_REQUEST_FORBIDDEN", "본인 피드백 요청만 취소할 수 있습니다."));
	}

	@ExceptionHandler(FeedbackRequestService.FeedbackRequestExistsException.class)
	public ResponseEntity<ResumeErrorResponse> feedbackRequestExists() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ResumeErrorResponse("FEEDBACK_REQUEST_EXISTS", "이 이력서는 이미 피드백을 요청했습니다."));
	}

	@ExceptionHandler(TargetResumeService.TargetResumeExistsException.class)
	public ResponseEntity<ResumeErrorResponse> targetResumeExists() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ResumeErrorResponse("TARGET_RESUME_EXISTS", "이 공고의 특화 이력서가 이미 있습니다."));
	}
}
