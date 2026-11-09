package com.newlecture.resuit.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.ProfileResponse;
import com.newlecture.resuit.dto.TalentPageResponse;
import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.entity.BadgeStatus;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.GithubContributionStat;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.repository.BestReviewRepository;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.CompanyOfferRepository;
import com.newlecture.resuit.repository.GithubContributionStatRepository;
import com.newlecture.resuit.repository.ReviewerActivityRepository;
import com.newlecture.resuit.repository.TalentSearchRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanySubscriptionService;
import com.newlecture.resuit.service.ReviewerBadgePolicy;
import com.newlecture.resuit.service.ReviewerBadgePolicy.EvaluationWindow;
import com.newlecture.resuit.service.TalentScoutingService;

@Service
@Transactional(readOnly = true)
public class TalentScoutingServiceImpl implements TalentScoutingService {

	private final CommonResumeRepository commonResumeRepository;
	private final TalentSearchRepository talentSearchRepository;
	private final CompanyMemberGuard companyGuard;
	private final CompanySubscriptionService subscriptionService;
	private final GithubContributionStatRepository contributionStats;
	// 배지 근거(채택 수, 서로 다른 요청자 수). 배지 진행도 화면과 같은 쿼리를 재사용한다.
	private final ReviewerActivityRepository reviewerActivities;
	private final BestReviewRepository bestReviews;
	private final CompanyOfferRepository offerRepository;

	public TalentScoutingServiceImpl(
			CommonResumeRepository commonResumeRepository,
			TalentSearchRepository talentSearchRepository,
			CompanyMemberGuard companyGuard,
			CompanySubscriptionService subscriptionService,
			GithubContributionStatRepository contributionStats,
			ReviewerActivityRepository reviewerActivities,
			BestReviewRepository bestReviews,
			CompanyOfferRepository offerRepository) {
		this.commonResumeRepository = commonResumeRepository;
		this.talentSearchRepository = talentSearchRepository;
		this.companyGuard = companyGuard;
		this.subscriptionService = subscriptionService;
		this.contributionStats = contributionStats;
		this.reviewerActivities = reviewerActivities;
		this.bestReviews = bestReviews;
		this.offerRepository = offerRepository;
	}

	@Override
	public TalentPageResponse getTalents(TalentSearchCondition condition, String username) {
		User company = companyGuard.requireCompany(username);
		// 상세 필터를 썼는데 Pro가 아니면 403이다. 분야(field)와 페이지만 쓰면 무료로 볼 수 있다.
		if (condition.hasDetailFilter() && !subscriptionService.isPro(company)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "상세 필터는 기업 Pro 요금제에서 사용할 수 있습니다.");
		}
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDate today = now.toLocalDate();
		Page<Long> ids = talentSearchRepository.searchResumeIds(condition, today, now);
		Map<Long, CommonResume> byId = commonResumeRepository.findByIdIn(ids.getContent()).stream()
				.collect(Collectors.toMap(CommonResume::getId, Function.identity()));
		List<CommonResume> resumes = ids.getContent().stream().map(byId::get).toList();
		List<UUID> userIds = resumes.stream().map(r -> r.getUser().getId()).toList();

		Map<UUID, Long> careerDays = talentSearchRepository.careerDays(userIds, today);
		Map<UUID, Long> postCounts = talentSearchRepository.postCounts(userIds);
		Map<UUID, Integer> contributions = contributionStats.findAllById(userIds).stream()
				.collect(Collectors.toMap(GithubContributionStat::getUserId, GithubContributionStat::getTotalContributions));
		Map<UUID, List<ProfileResponse.BadgeSummary>> badges = talentSearchRepository.badges(userIds).stream()
				.collect(Collectors.groupingBy(ub -> ub.getUser().getId(),
						Collectors.mapping(ub -> toBadgeSummary(ub, now), Collectors.toList())));
		Map<UUID, List<ProfileResponse.BadgeHistorySummary>> history = talentSearchRepository.badgeHistory(userIds).stream()
				.collect(Collectors.groupingBy(h -> h.getUser().getId(),
						Collectors.mapping(h -> new ProfileResponse.BadgeHistorySummary(h.getBadge().getId(),
								h.getBadge().getName(), h.getActiveFrom(), h.getActiveUntil()), Collectors.toList())));
		// 이 기업이 이미 제안을 보낸 회원
		Set<UUID> offered = userIds.isEmpty() ? Set.of()
				: new HashSet<>(offerRepository.findOfferedUserIds(company.getId(), userIds));
		// 배지 근거를 셀 기간. 배지 평가와 같은 6개월 창이다.
		EvaluationWindow window = ReviewerBadgePolicy.windowEndingAt(now, false);

		List<TalentSummary> content = resumes.stream().map(resume -> {
			UUID id = resume.getUser().getId();
			List<ProfileResponse.BadgeSummary> owned = badges.getOrDefault(id, List.of());
			// 배지가 있는 회원만 근거를 센다. 배지가 없으면 근거를 보여 주지 않는다.
			ReviewerBadgePolicy.Activity activity = owned.isEmpty() ? null : new ReviewerBadgePolicy.Activity(
					reviewerActivities.countAdopted(id, window.from(), window.to()),
					reviewerActivities.countDistinctRequesters(id, window.from(), window.to()),
					bestReviews.countForReviewer(id, window.from().toLocalDate(), window.to().toLocalDate()));
			return TalentSummary.of(resume,
					careerLevel(careerDays.getOrDefault(id, 0L)),
					contributions.getOrDefault(id, 0),
					postCounts.getOrDefault(id, 0L),
					owned, history.getOrDefault(id, List.of()), activity, offered.contains(id));
		}).toList();

		// 분야 탭 옆에 보여 줄 인원 수(상세 필터 반영)
		Map<String, Long> fieldCounts = talentSearchRepository.countByField(condition, today, now);
		return new TalentPageResponse(content, ids.getNumber(), ids.getSize(), ids.getTotalElements(),
				ids.hasNext(), fieldCounts);
	}

	private static String careerLevel(long days) {
		if (days < 365) {
			return "ENTRY";
		}
		return days < 365 * 3 ? "ONE_TO_THREE" : "THREE_PLUS";
	}

	// 회원 배지를 프로필 응답과 같은 모양으로 바꾼다. 연속 분기 수는 ACTIVE일 때만 센다.
	private static ProfileResponse.BadgeSummary toBadgeSummary(UserBadge owned, LocalDateTime now) {
		int streak = owned.getStatus() == BadgeStatus.ACTIVE
				? ReviewerBadgePolicy.streakQuarters(owned.getContinuousSince(), now)
				: 0;
		return new ProfileResponse.BadgeSummary(owned.getBadge().getId(), owned.getBadge().getCode(),
				owned.getBadge().getName(), owned.getBadge().getIconUrl(), owned.getStatus(), owned.getAcquiredAt(),
				owned.getGraceUntil(), owned.getExpiresAt(), owned.getContinuousSince(), streak);
	}

	@Override
	public CommonResumeDetailResponse getPublicResume(TalentResumeQuery query) {
		companyGuard.requireCompany(query.username());
		return commonResumeRepository.findByIdAndUserIdAndIsPublicTrue(query.resumeId(), query.userId())
				.map(CommonResumeDetailResponse::from)
				// 비공개 이력서의 존재를 알 수 없도록 없는 경우와 똑같이 404로 끝낸다.
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
}
