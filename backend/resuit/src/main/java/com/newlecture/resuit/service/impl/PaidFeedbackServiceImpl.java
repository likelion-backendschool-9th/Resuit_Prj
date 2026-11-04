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

	public PaidFeedbackServiceImpl(
			PaidFeedbackRequestRepository paidFeedbackRequestRepository,
			PaidFeedbackResponseRepository paidFeedbackResponseRepository,
			UserRepository userRepository,
			MentorRepository mentorRepository,
			CommonResumeRepository commonResumeRepository,
			TargetResumeRepository targetResumeRepository,
			CreditService creditService) {
		this.paidFeedbackRequestRepository = paidFeedbackRequestRepository;
		this.paidFeedbackResponseRepository = paidFeedbackResponseRepository;
		this.userRepository = userRepository;
		this.mentorRepository = mentorRepository;
		this.commonResumeRepository = commonResumeRepository;
		this.targetResumeRepository = targetResumeRepository;
		this.creditService = creditService;
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

		Mentor mentor = mentorRepository.findById(request.mentorId())
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

		return PaidFeedbackResponseSummary.from(paidFeedbackResponseRepository.save(PaidFeedbackResponse.builder()
				.request(paidFeedbackRequest)
				.content(command.content())
				.build()));
	}

	private User findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
