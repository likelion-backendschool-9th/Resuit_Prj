package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackRequest;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

// 피드백 게시판(4.x) 조회 전용. 채택에서 쓰는 FeedbackRequestRepository·FeedbackCommentRepository는 건드리지 않는다.
public interface FeedbackBoardRepository extends Repository<FeedbackRequest, Long> {

    // 취소하지 않은 요청만. category·username이 null이면 그 조건은 걸지 않는다.
    @Query("""
        SELECT r FROM FeedbackRequest r JOIN FETCH r.user u
        LEFT JOIN FETCH r.commonResume
        LEFT JOIN FETCH r.targetResume t LEFT JOIN FETCH t.job j LEFT JOIN FETCH j.company
        WHERE r.deletedAt IS NULL
          AND (:category IS NULL OR r.category = :category)
          AND (:username IS NULL OR u.username = :username)
        ORDER BY r.createdAt DESC, r.id DESC
        """)
    List<FeedbackRequest> findActive(@Param("category") String category, @Param("username") String username);

    @Query("""
        SELECT s.request.id AS requestId, s.sectionType AS sectionType FROM FeedbackRequestSection s
        WHERE s.request.id IN :requestIds ORDER BY s.sectionId
        """)
    List<SectionRow> findSections(@Param("requestIds") Collection<Long> requestIds);

    @Query("""
        SELECT c.feedbackRequest.id AS requestId, COUNT(c) AS count,
               SUM(CASE WHEN c.isAdopted = true THEN 1 ELSE 0 END) AS adoptedCount
        FROM FeedbackComment c
        WHERE c.feedbackRequest.id IN :requestIds
        GROUP BY c.feedbackRequest.id
        """)
    List<CommentCountRow> countComments(@Param("requestIds") Collection<Long> requestIds);

    interface SectionRow {
        Long getRequestId();
        String getSectionType();
    }

    interface CommentCountRow {
        Long getRequestId();
        long getCount();
        long getAdoptedCount();
    }
}
