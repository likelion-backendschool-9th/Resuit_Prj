package com.newlecture.resuit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);
}
