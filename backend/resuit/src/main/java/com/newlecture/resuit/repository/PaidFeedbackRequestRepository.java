package com.newlecture.resuit.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.PaidFeedbackRequest;

public interface PaidFeedbackRequestRepository extends JpaRepository<PaidFeedbackRequest, Long> {

	long countByMentor_UserIdAndStatus(UUID mentorId, String status);

	boolean existsByIdAndStatus(Long id, String status);

	@EntityGraph(attributePaths = { "requester", "mentor", "mentor.user", "commonResume", "targetResume" })
	@Query("""
			select r from PaidFeedbackRequest r
			where r.requester.id = :userId or r.mentor.userId = :userId
			order by r.id desc
			""")
	Page<PaidFeedbackRequest> findAllForUser(@Param("userId") UUID userId, Pageable pageable);

	@EntityGraph(attributePaths = { "requester", "mentor", "mentor.user", "commonResume", "targetResume" })
	@Query("""
			select r from PaidFeedbackRequest r
			where (r.requester.id = :userId or r.mentor.userId = :userId) and r.status = :status
			order by r.id desc
			""")
	Page<PaidFeedbackRequest> findAllForUserByStatus(
			@Param("userId") UUID userId,
			@Param("status") String status,
			Pageable pageable);

	// 취소와 답변이 동시에 와도 하나만 성공하도록 예상 상태를 조건으로 건다.
	@Modifying
	@Query("""
			update PaidFeedbackRequest r
			set r.status = 'COMPLETED', r.updatedAt = :now
			where r.id = :id and r.status in ('REQUESTED', 'IN_PROGRESS')
			""")
	int complete(@Param("id") Long id, @Param("now") LocalDateTime now);
}
