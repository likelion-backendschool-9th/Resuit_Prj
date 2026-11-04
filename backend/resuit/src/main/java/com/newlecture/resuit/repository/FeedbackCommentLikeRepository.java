package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackCommentLike;
import com.newlecture.resuit.entity.FeedbackCommentLikeId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackCommentLikeRepository extends JpaRepository<FeedbackCommentLike, FeedbackCommentLikeId> {
    @Modifying
    @Query(value = """
        INSERT INTO feedback_comment_likes (comment_id, user_id, created_at)
        VALUES (:commentId, :userId, CURRENT_TIMESTAMP)
        ON CONFLICT (comment_id, user_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("commentId") Long commentId, @Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM FeedbackCommentLike l WHERE l.id.commentId = :commentId AND l.id.userId = :userId")
    int deleteLike(@Param("commentId") Long commentId, @Param("userId") UUID userId);
}
