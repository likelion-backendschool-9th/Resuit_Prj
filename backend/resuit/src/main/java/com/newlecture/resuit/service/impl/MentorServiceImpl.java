package com.newlecture.resuit.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorSummary;
import com.newlecture.resuit.entity.Mentor;
import com.newlecture.resuit.repository.FeedbackCommentRepository;
import com.newlecture.resuit.repository.MentorRepository;
import com.newlecture.resuit.repository.PaidFeedbackRequestRepository;
import com.newlecture.resuit.service.MentorService;

@Service
@Transactional(readOnly = true)
public class MentorServiceImpl implements MentorService {

	private static final String COMPLETED = "COMPLETED";

	private final MentorRepository mentorRepository;
	private final PaidFeedbackRequestRepository paidFeedbackRequestRepository;
	private final FeedbackCommentRepository feedbackCommentRepository;

	public MentorServiceImpl(
			MentorRepository mentorRepository,
			PaidFeedbackRequestRepository paidFeedbackRequestRepository,
			FeedbackCommentRepository feedbackCommentRepository) {
		this.mentorRepository = mentorRepository;
		this.paidFeedbackRequestRepository = paidFeedbackRequestRepository;
		this.feedbackCommentRepository = feedbackCommentRepository;
	}

	@Override
	public Page<MentorSummary> getMentors(Pageable pageable) {
		return mentorRepository.findAllByOrderByVerifiedAtDescUserIdDesc(pageable)
				.map(MentorSummary::from);
	}

	@Override
	public MentorDetailResponse getMentor(UUID id) {
		Mentor mentor = mentorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		MentorDetailResponse.ReviewHistory reviewHistory = new MentorDetailResponse.ReviewHistory(
				paidFeedbackRequestRepository.countByMentor_UserIdAndStatus(id, COMPLETED),
				feedbackCommentRepository.countByUser_IdAndFeedbackRequest_DeletedAtIsNull(id),
				feedbackCommentRepository.countByUser_IdAndIsAdoptedTrueAndFeedbackRequest_DeletedAtIsNull(id),
				feedbackCommentRepository
						.findTop5ByUser_IdAndFeedbackRequest_DeletedAtIsNullOrderByCreatedAtDescCommentIdDesc(id)
						.stream()
						.map(MentorDetailResponse.RecentReview::from)
						.toList());

		return MentorDetailResponse.of(mentor, reviewHistory);
	}
}
