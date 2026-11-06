package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.TalentScoutingService;

@Service
@Transactional(readOnly = true)
public class TalentScoutingServiceImpl implements TalentScoutingService {

	private static final String COMPANY = "COMPANY";

	private final CommonResumeRepository commonResumeRepository;
	private final UserRepository userRepository;

	public TalentScoutingServiceImpl(
			CommonResumeRepository commonResumeRepository,
			UserRepository userRepository) {
		this.commonResumeRepository = commonResumeRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Page<TalentSummary> getTalents(TalentSearchCondition condition, String username) {
		requireCompany(username);
		Page<CommonResume> resumes = condition.field() == null
				? commonResumeRepository.findLatestPublic(condition.toPageable())
				: commonResumeRepository.findLatestPublicByField(condition.field(), condition.toPageable());
		return resumes.map(TalentSummary::from);
	}

	@Override
	public CommonResumeDetailResponse getPublicResume(TalentResumeQuery query) {
		requireCompany(query.username());
		return commonResumeRepository.findByIdAndUserIdAndIsPublicTrue(query.resumeId(), query.userId())
				.map(CommonResumeDetailResponse::from)
				// 비공개 이력서의 존재를 알 수 없도록 없는 경우와 똑같이 404로 끝낸다.
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	// 기업 회원 판별은 이 메서드 하나에만 둔다. 기준이 바뀌면 여기만 고치면 된다.
	private void requireCompany(String username) {
		String role = userRepository.findByUsername(username)
				.map(user -> user.getRole())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		if (!COMPANY.equals(role)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
	}
}
