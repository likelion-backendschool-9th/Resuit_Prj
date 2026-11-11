package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.resume.ResumePreviewResponse;

import org.springframework.data.domain.Page;

import com.newlecture.resuit.dto.PaidFeedbackCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackDetailResponse;
import com.newlecture.resuit.dto.PaidFeedbackResponseCommand;
import com.newlecture.resuit.dto.PaidFeedbackResponseSummary;
import com.newlecture.resuit.dto.PaidFeedbackSearchCondition;
import com.newlecture.resuit.dto.PaidFeedbackSummary;

public interface PaidFeedbackService {

	PaidFeedbackSummary create(PaidFeedbackCreateRequest request, String username);

	Page<PaidFeedbackSummary> getRequests(PaidFeedbackSearchCondition condition, String username);

	PaidFeedbackDetailResponse getRequest(Long id, String username);

	PaidFeedbackResponseSummary createResponse(PaidFeedbackResponseCommand command);

	// 요청에 붙은 이력서를 돌려준다. 요청자와 그 요청의 현직자만 볼 수 있다.
	ResumePreviewResponse getRequestResume(Long id, String username);
}
