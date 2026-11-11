package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackComment;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewerActivityRepository extends JpaRepository<FeedbackComment, Long> {

    @Query("""
        select count(c) from FeedbackComment c
        where c.user.id = :userId and c.isAdopted = true
          and c.adoptedAt >= :from and c.adoptedAt < :to
        """)
    long countAdopted(@Param("userId") UUID userId,
                      @Param("from") LocalDateTime from,
                      @Param("to") LocalDateTime to);

    @Query("""
        select count(distinct c.feedbackRequest.user.id) from FeedbackComment c
        where c.user.id = :userId and c.isAdopted = true
          and c.adoptedAt >= :from and c.adoptedAt < :to
        """)
    long countDistinctRequesters(@Param("userId") UUID userId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);
}
