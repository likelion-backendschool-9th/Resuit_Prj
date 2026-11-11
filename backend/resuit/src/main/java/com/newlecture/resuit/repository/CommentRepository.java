package com.newlecture.resuit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);

	@org.springframework.data.jpa.repository.Query("""
			select c.post.id as postId, count(c.id) as count
			from Comment c
			where c.post.id in :postIds and c.deletedAt is null
			group by c.post.id
			""")
	List<PostCount> countActiveByPostIds(@org.springframework.data.repository.query.Param("postIds") List<Long> postIds);

	interface PostCount {
		Long getPostId();
		long getCount();
	}
}
