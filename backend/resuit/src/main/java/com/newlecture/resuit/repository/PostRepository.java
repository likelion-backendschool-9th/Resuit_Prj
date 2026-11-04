package com.newlecture.resuit.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}