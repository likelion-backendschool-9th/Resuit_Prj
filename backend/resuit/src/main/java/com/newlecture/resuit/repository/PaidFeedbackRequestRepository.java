package com.newlecture.resuit.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.PaidFeedbackRequest;

public interface PaidFeedbackRequestRepository extends JpaRepository<PaidFeedbackRequest, Long> {

	long countByMentor_UserIdAndStatus(UUID mentorId, String status);
}
