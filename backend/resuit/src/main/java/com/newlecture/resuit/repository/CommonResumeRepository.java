package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CommonResume;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommonResumeRepository extends JpaRepository<CommonResume, Long> {

    List<CommonResume> findByUserIdOrderByIdAsc(UUID userId);

    // 목록 카드의 "받은 피드백 N개". 취소한 요청에 달린 피드백도 받은 피드백으로 센다.
    @Query("""
        SELECT c.feedbackRequest.commonResume.id AS resumeId, COUNT(c) AS count
        FROM FeedbackComment c
        WHERE c.feedbackRequest.commonResume.user.id = :userId
        GROUP BY c.feedbackRequest.commonResume.id
        """)
    List<CountRow> countFeedbackByUser(@Param("userId") UUID userId);

    // 목록 카드의 "특화 이력서 N개의 베이스"
    @Query("""
        SELECT t.commonResume.id AS resumeId, COUNT(t) AS count
        FROM TargetResume t
        WHERE t.commonResume.user.id = :userId
        GROUP BY t.commonResume.id
        """)
    List<CountRow> countTargetResumesByUser(@Param("userId") UUID userId);

    interface CountRow {
        Long getResumeId();
        long getCount();
    }
}
