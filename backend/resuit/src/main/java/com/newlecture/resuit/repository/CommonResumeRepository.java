package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CommonResume;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommonResumeRepository extends JpaRepository<CommonResume, Long> {

    List<CommonResume> findByUserIdOrderByIdAsc(UUID userId);

    List<CommonResume> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<CommonResume> findByIdAndUserId(Long id, UUID userId);

    Optional<CommonResume> findByIdAndUserIdAndIsPublicTrue(Long id, UUID userId);

    // 이 회원에게 공개 이력서가 하나라도 있는지 확인한다. 채용 제안은 인재 찾기에 보이는 회원(공개 이력서 있음)에게만 보낸다.
    boolean existsByUserIdAndIsPublicTrue(UUID userId);

    @EntityGraph(attributePaths = "user")
    // IN 조회는 순서를 보장하지 않으므로, 서비스에서 검색 결과 순서대로 다시 정렬한다.
    List<CommonResume> findByIdIn(Collection<Long> ids);

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

    // 취소하지 않은 피드백 요청. 이력서 하나에 진행 중인 요청은 하나만 둔다.
    @Query("""
        SELECT COUNT(r) > 0 FROM FeedbackRequest r
        WHERE r.commonResume.id = :resumeId AND r.deletedAt IS NULL
        """)
    boolean hasActiveFeedbackRequest(@Param("resumeId") Long resumeId);

    @Query("""
        SELECT r.commonResume.id AS resumeId, r.id AS requestId FROM FeedbackRequest r
        WHERE r.user.id = :userId AND r.commonResume IS NOT NULL AND r.deletedAt IS NULL
        """)
    List<ActiveRequestRow> findActiveFeedbackRequests(@Param("userId") UUID userId);

    interface CountRow {
        Long getResumeId();
        long getCount();
    }

    interface ActiveRequestRow {
        Long getResumeId();
        Long getRequestId();
    }
}
