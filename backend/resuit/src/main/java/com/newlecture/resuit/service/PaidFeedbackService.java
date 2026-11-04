package com.newlecture.resuit.service;

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
}
