package com.newlecture.resuit.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorProfileRequest;
import com.newlecture.resuit.dto.MentorSummary;
import com.newlecture.resuit.dto.MyMentorResponse;
import com.newlecture.resuit.entity.CompanyEmailDomain;
import com.newlecture.resuit.entity.Mentor;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.Verification;
import com.newlecture.resuit.repository.CompanyEmailDomainRepository;
import com.newlecture.resuit.repository.FeedbackCommentRepository;
import com.newlecture.resuit.repository.MentorRepository;
import com.newlecture.resuit.repository.PaidFeedbackRequestRepository;
import com.newlecture.resuit.repository.TalentSearchRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.VerificationRepository;
import com.newlecture.resuit.service.MentorService;

@Service
@Transactional(readOnly = true)
public class MentorServiceImpl implements MentorService {

	private static final String COMPLETED = "COMPLETED";
	private static final int YEAR_DAYS = 365;

	private final MentorRepository mentorRepository;
	private final PaidFeedbackRequestRepository paidFeedbackRequestRepository;
	private final FeedbackCommentRepository feedbackCommentRepository;
	private final UserRepository userRepository;
	private final VerificationRepository verificationRepository;
	private final CompanyEmailDomainRepository companyDomainRepository;
	// 프로필 경력 일수 계산. 인재 찾기 경력 필터와 같은 계산식(겹친 기간은 한 번만)을 쓴다.
	private final TalentSearchRepository talentSearchRepository;
	// 현직자 인증 기록의 종류(CompanyVerificationServiceImpl과 같은 값)
	private static final String VERIFICATION_TYPE = "company_email";

	public MentorServiceImpl(
			MentorRepository mentorRepository,
			PaidFeedbackRequestRepository paidFeedbackRequestRepository,
			FeedbackCommentRepository feedbackCommentRepository,
			UserRepository userRepository,
			VerificationRepository verificationRepository,
			CompanyEmailDomainRepository companyDomainRepository,
			TalentSearchRepository talentSearchRepository) {
		this.mentorRepository = mentorRepository;
		this.paidFeedbackRequestRepository = paidFeedbackRequestRepository;
		this.feedbackCommentRepository = feedbackCommentRepository;
		this.userRepository = userRepository;
		this.verificationRepository = verificationRepository;
		this.companyDomainRepository = companyDomainRepository;
		this.talentSearchRepository = talentSearchRepository;
	}

	@Override
	public Page<MentorSummary> getMentors(Pageable pageable) {
		// 인증이 유효한 현직자를 인증일 최신순으로 읽고, 한 페이지의 경력을 한 번에 계산한다.
		Page<Mentor> page = mentorRepository.findActive(pageable);
		Map<UUID, Long> careerDays = talentSearchRepository.careerDays(
				page.getContent().stream().map(Mentor::getUserId).toList(), LocalDate.now());
		return page.map(mentor -> MentorSummary.of(mentor, careerYears(careerDays.get(mentor.getUserId()))));
	}

	@Override
	public MentorDetailResponse getMentor(UUID id) {
		Mentor mentor = mentorRepository.findActiveById(id)
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

		Long days = talentSearchRepository.careerDays(List.of(id), LocalDate.now()).get(id);
		return MentorDetailResponse.of(mentor, careerYears(days), reviewHistory);
	}

	// 내 현직자 인증 상태와 현직자 정보를 돌려준다.
	@Override
	public MyMentorResponse getMyMentor(String username) {
		User me = findUser(username);
		Verification verification = activeVerification(me).orElse(null);
		Mentor mentor = mentorRepository.findById(me.getId()).orElse(null);
		return toMyResponse(verification, mentor);
	}

	@Override
	@Transactional
	public MyMentorResponse saveMyMentor(String username, MentorProfileRequest request) {
		User me = findUser(username);
		// 회사 이메일 인증이 유효해야 현직자가 될 수 있다.
		Verification verification = activeVerification(me)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "회사 이메일로 현직자 인증을 먼저 해 주세요."));
		String jobTitle = request.jobTitle().trim();
		Mentor mentor = mentorRepository.findById(me.getId()).orElse(null);
		if (mentor == null) {
			mentor = mentorRepository.save(Mentor.builder()
					.user(me)
					.companyName(companyName(verification))
					.jobTitle(jobTitle)
					.priceCredits(request.priceCredits())
					.verifiedAt(verification.getVerifiedAt())
					.build());
		} else {
			mentor.updateProfile(jobTitle, request.priceCredits());
		}
		return toMyResponse(verification, mentor);
	}

	// 경력 일수를 만 연수로 바꾼다. 프로필 경력이 없으면(0일) null이라 화면에 연차를 표시하지 않는다.
	private static Integer careerYears(Long days) {
		return days == null || days <= 0 ? null : (int) (days / YEAR_DAYS);
	}

	private User findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	// 취소하지 않은 가장 최근 회사 이메일 인증
	private java.util.Optional<Verification> activeVerification(User user) {
		return verificationRepository.findFirstByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNullOrderByVerifiedAtDesc(
				user.getId(), VERIFICATION_TYPE);
	}

	// 인증한 이메일 도메인의 회사명. 등록하지 않은 회사명이면 도메인을 그대로 쓴다.
	private String companyName(Verification verification) {
		String domain = verification.getEmail().substring(verification.getEmail().indexOf('@') + 1);
		return companyDomainRepository.findByDomain(domain)
				.map(CompanyEmailDomain::getCompanyName)
				.filter(name -> !name.isBlank())
				.orElse(domain);
	}

	private MyMentorResponse toMyResponse(Verification verification, Mentor mentor) {
		boolean verified = verification != null;
		return new MyMentorResponse(verified,
				verified ? verification.getEmail() : null,
				verified ? companyName(verification) : null,
				verified && mentor != null,
				mentor == null ? null : mentor.getJobTitle(),
				mentor == null ? null : mentor.getPriceCredits(),
				verified ? verification.getVerifiedAt() : null);
	}
}
