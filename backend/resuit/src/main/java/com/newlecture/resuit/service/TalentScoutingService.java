package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;

import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;

public interface TalentScoutingService {

	Page<TalentSummary> getTalents(TalentSearchCondition condition, String username);
}
