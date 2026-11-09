package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackRequest;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRequestRepository extends JpaRepository<FeedbackRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<FeedbackRequest> findByCommonResume_IdOrderByIdAsc(Long resumeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM FeedbackRequest r WHERE r.id = :id")
    Optional<FeedbackRequest> findForAcceptance(@Param("id") Long id);
}
