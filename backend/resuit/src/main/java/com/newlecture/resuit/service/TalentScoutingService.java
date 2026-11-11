package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.TalentPageResponse;
import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;

public interface TalentScoutingService {

	TalentPageResponse getTalents(TalentSearchCondition condition, String username);

	CommonResumeDetailResponse getPublicResume(TalentResumeQuery query);
}
