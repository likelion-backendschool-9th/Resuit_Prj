package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorSummary;

public interface MentorService {

	Page<MentorSummary> getMentors(Pageable pageable);

	MentorDetailResponse getMentor(UUID id);
}
