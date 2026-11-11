package com.newlecture.resuit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.PostLike;
import com.newlecture.resuit.entity.PostLikeId;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    @org.springframework.data.jpa.repository.Query("""
            select l.post.id as postId, count(l.id) as count
            from PostLike l
            where l.post.id in :postIds
            group by l.post.id
            """)
    java.util.List<PostCount> countByPostIds(@org.springframework.data.repository.query.Param("postIds") java.util.List<Long> postIds);

    interface PostCount {
        Long getPostId();
        long getCount();
    }

    @org.springframework.data.jpa.repository.Query("select l.post.id from PostLike l where l.user.username = :username and l.post.id in :postIds")
    java.util.Set<Long> findLikedPostIds(@org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("postIds") java.util.List<Long> postIds);
}
