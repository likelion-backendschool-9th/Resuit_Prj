package com.newlecture.resuit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.PaidFeedbackResponse;

public interface PaidFeedbackResponseRepository extends JpaRepository<PaidFeedbackResponse, Long> {

	List<PaidFeedbackResponse> findByRequest_IdOrderByIdAsc(Long requestId);
}
