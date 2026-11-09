package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorProfileRequest;
import com.newlecture.resuit.dto.MyMentorResponse;
import com.newlecture.resuit.dto.MentorSummary;

public interface MentorService {

	Page<MentorSummary> getMentors(Pageable pageable);

	MentorDetailResponse getMentor(UUID id);
	// 내 현직자 인증 상태와 현직자 정보
	MyMentorResponse getMyMentor(String username);
	// 현직자 정보를 저장한다. 회사 이메일 인증이 유효해야 하고, 처음 저장하면 현직자 목록에 나온다.
	MyMentorResponse saveMyMentor(String username, MentorProfileRequest request);
}
