package com.newlecture.resuit.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.PaidFeedbackCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackDetailResponse;
import com.newlecture.resuit.dto.PaidFeedbackResponseCommand;
import com.newlecture.resuit.dto.PaidFeedbackResponseSummary;
import com.newlecture.resuit.dto.PaidFeedbackSearchCondition;
import com.newlecture.resuit.dto.PaidFeedbackSummary;
import com.newlecture.resuit.dto.notification.NotificationCommand;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.Mentor;
import com.newlecture.resuit.entity.PaidFeedbackRequest;
import com.newlecture.resuit.entity.PaidFeedbackResponse;
import com.newlecture.resuit.entity.TargetResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.MentorRepository;
import com.newlecture.resuit.repository.PaidFeedbackRequestRepository;
import com.newlecture.resuit.repository.PaidFeedbackResponseRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CreditService;
import com.newlecture.resuit.service.NotificationService;
import com.newlecture.resuit.service.NotificationType;
import com.newlecture.resuit.service.PaidFeedbackService;

@Service
@Transactional(readOnly = true)
public class PaidFeedbackServiceImpl implements PaidFeedbackService {

	private final PaidFeedbackRequestRepository paidFeedbackRequestRepository;
	private final PaidFeedbackResponseRepository paidFeedbackResponseRepository;
	private final UserRepository userRepository;
	private final MentorRepository mentorRepository;
	private final CommonResumeRepository commonResumeRepository;
	private final TargetResumeRepository targetResumeRepository;
	private final CreditService creditService;
	private final NotificationService notificationService;

	public PaidFeedbackServiceImpl(
			PaidFeedbackRequestRepository paidFeedbackRequestRepository,
			PaidFeedbackResponseRepository paidFeedbackResponseRepository,
			UserRepository userRepository,
			MentorRepository mentorRepository,
			CommonResumeRepository commonResumeRepository,
			TargetResumeRepository targetResumeRepository,
			CreditService creditService,
			NotificationService notificationService) {
		this.paidFeedbackRequestRepository = paidFeedbackRequestRepository;
		this.paidFeedbackResponseRepository = paidFeedbackResponseRepository;
		this.userRepository = userRepository;
		this.mentorRepository = mentorRepository;
		this.commonResumeRepository = commonResumeRepository;
		this.targetResumeRepository = targetResumeRepository;
		this.creditService = creditService;
		this.notificationService = notificationService;
	}

	@Override
	@Transactional
	public PaidFeedbackSummary create(PaidFeedbackCreateRequest request, String username) {
		if ((request.commonResumeId() == null) == (request.targetResumeId() == null)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이력서는 하나만 선택해야 합니다.");
		}
		User requester = findUser(username);
		if (requester.getId().equals(request.mentorId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게는 요청할 수 없습니다.");
		}

		Mentor mentor = mentorRepository.findActiveById(request.mentorId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		CommonResume commonResume = findCommonResume(request.commonResumeId(), requester.getId());
		TargetResume targetResume = findTargetResume(request.targetResumeId(), requester.getId());

		PaidFeedbackRequest paidFeedbackRequest = paidFeedbackRequestRepository.save(PaidFeedbackRequest.builder()
				.requester(requester)
				.mentor(mentor)
				.commonResume(commonResume)
				.targetResume(targetResume)
				.content(request.content())
				// 지금 가격을 저장한다. 현직자가 나중에 가격을 바꿔도 이 요청의 금액은 그대로다.
				.credits(mentor.getPriceCredits())
				.status("REQUESTED")
				.build());
		creditService.use(paidFeedbackRequest);
		// 현직자에게 요청이 왔다고 알린다.
		notificationService.notify(new NotificationCommand(
				mentor.getUserId(), requester.getId(), NotificationType.PAID_FEEDBACK_REQUESTED,
				NotificationService.nameOf(requester) + " 님이 유료 피드백을 요청했어요.",
				"/paid-feedback/" + paidFeedbackRequest.getId()));

		return PaidFeedbackSummary.from(paidFeedbackRequest);
	}

	@Override
	public Page<PaidFeedbackSummary> getRequests(PaidFeedbackSearchCondition condition, String username) {
		UUID userId = findUser(username).getId();
		Page<PaidFeedbackRequest> requests = condition.status() == null
				? paidFeedbackRequestRepository.findAllForUser(userId, condition.toPageable())
				: paidFeedbackRequestRepository.findAllForUserByStatus(
						userId, condition.status(), condition.toPageable());
		return requests.map(PaidFeedbackSummary::from);
	}

	@Override
	public PaidFeedbackDetailResponse getRequest(Long id, String username) {
		PaidFeedbackRequest request = findRequest(id);
		if (!request.getRequester().getUsername().equals(username) && !isMentor(request, username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return PaidFeedbackDetailResponse.of(
				request,
				paidFeedbackResponseRepository.findByRequest_IdOrderByIdAsc(id).stream()
						.map(PaidFeedbackResponseSummary::from)
						.toList());
	}

	@Override
	@Transactional
	public PaidFeedbackResponseSummary createResponse(PaidFeedbackResponseCommand command) {
		Long id = command.requestId();
		PaidFeedbackRequest paidFeedbackRequest = findRequest(id);
		if (!isMentor(paidFeedbackRequest, command.username())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		// 요청 하나에 답변이 여러 개 달릴 수 있지만 적립은 처음 완료될 때 한 번만 한다.
		if (paidFeedbackRequestRepository.complete(id, LocalDateTime.now()) == 1) {
			creditService.earn(paidFeedbackRequest);
		} else if (!paidFeedbackRequestRepository.existsByIdAndStatus(id, "COMPLETED")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "취소된 요청에는 답변할 수 없습니다.");
		}

		PaidFeedbackResponse response = paidFeedbackResponseRepository.save(PaidFeedbackResponse.builder()
				.request(paidFeedbackRequest)
				.content(command.content())
				.build());
		// 요청자에게 답변이 왔다고 알린다.
		User mentorUser = paidFeedbackRequest.getMentor().getUser();
		notificationService.notify(new NotificationCommand(
				paidFeedbackRequest.getRequester().getId(), mentorUser.getId(), NotificationType.PAID_FEEDBACK_ANSWERED,
				NotificationService.nameOf(mentorUser) + " 님이 유료 피드백 답변을 남겼어요.",
				"/paid-feedback/" + id));
		return PaidFeedbackResponseSummary.from(response);
	}

	private User findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	// 요청에 붙은 이력서. 현직자가 피드백을 쓰려면 요청자의 이력서를 봐야 하는데,
	// 이력서 미리보기(GET /resumes/{id})는 공개 이력서나 게시판 요청에 올린 이력서만 보여 주므로 따로 둔다.
	@Override
	public ResumePreviewResponse getRequestResume(Long id, String username) {
		PaidFeedbackRequest request = findRequest(id);
		// 요청자와 그 요청의 현직자만 볼 수 있다.
		if (!request.getRequester().getUsername().equals(username) && !isMentor(request, username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		// 공통·특화 이력서 중 요청에 붙은 쪽을 피드백 게시판 미리보기와 같은 모양으로 돌려준다.
		if (request.getCommonResume() != null) {
			return ResumePreviewResponse.from(request.getCommonResume());
		}
		TargetResume resume = request.getTargetResume();
		return ResumePreviewResponse.of(resume, targetResumeRepository.findQuestions(resume.getJob().getId()));
	}

	private boolean isMentor(PaidFeedbackRequest request, String username) {
		return request.getMentor().getUser().getUsername().equals(username);
	}

	private PaidFeedbackRequest findRequest(Long id) {
		return paidFeedbackRequestRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	private CommonResume findCommonResume(Long id, UUID userId) {
		if (id == null) {
			return null;
		}
		CommonResume resume = commonResumeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!resume.getUser().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return resume;
	}

	private TargetResume findTargetResume(Long id, UUID userId) {
		if (id == null) {
			return null;
		}
		TargetResume resume = targetResumeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!resume.getUser().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return resume;
	}
}
