package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "feedback_comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackCommentLike extends BaseCreatedEntity {

    @EmbeddedId
    private FeedbackCommentLikeId id;

    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private FeedbackComment comment;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FeedbackCommentLike(FeedbackComment comment, User user) {
        this.id = new FeedbackCommentLikeId(comment.getCommentId(), user.getId());
        this.comment = comment;
        this.user = user;
    }
}
