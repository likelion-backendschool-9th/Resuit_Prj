package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;

import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;

public interface TalentScoutingService {

	Page<TalentSummary> getTalents(TalentSearchCondition condition, String username);

	CommonResumeDetailResponse getPublicResume(TalentResumeQuery query);
}
