package com.newlecture.resuit.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.TalentSearchRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanySubscriptionService;
import com.newlecture.resuit.service.TalentScoutingService;

@Service
@Transactional(readOnly = true)
public class TalentScoutingServiceImpl implements TalentScoutingService {

	private final CommonResumeRepository commonResumeRepository;
	private final TalentSearchRepository talentSearchRepository;
	private final CompanyMemberGuard companyGuard;
	private final CompanySubscriptionService subscriptionService;

	public TalentScoutingServiceImpl(
			CommonResumeRepository commonResumeRepository,
			TalentSearchRepository talentSearchRepository,
			CompanyMemberGuard companyGuard,
			CompanySubscriptionService subscriptionService) {
		this.commonResumeRepository = commonResumeRepository;
		this.talentSearchRepository = talentSearchRepository;
		this.companyGuard = companyGuard;
		this.subscriptionService = subscriptionService;
	}

	@Override
	public Page<TalentSummary> getTalents(TalentSearchCondition condition, String username) {
		User company = companyGuard.requireCompany(username);
		// 상세 필터를 썼는데 Pro가 아니면 403이다. 분야(field)와 페이지만 쓰면 무료로 볼 수 있다.
		if (condition.hasDetailFilter() && !subscriptionService.isPro(company)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "상세 필터는 기업 Pro 요금제에서 사용할 수 있습니다.");
		}
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		Page<Long> ids = talentSearchRepository.searchResumeIds(condition, now.toLocalDate(), now);
		Map<Long, CommonResume> byId = commonResumeRepository.findByIdIn(ids.getContent()).stream()
				.collect(Collectors.toMap(CommonResume::getId, Function.identity()));
		// 검색 결과 순서(최신순)대로 응답 DTO로 바꾼다. 페이지 정보는 그대로 유지한다.
		List<TalentSummary> content = ids.getContent().stream()
				.map(byId::get)
				.map(TalentSummary::from)
				.toList();
		return new PageImpl<>(content, ids.getPageable(), ids.getTotalElements());
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
