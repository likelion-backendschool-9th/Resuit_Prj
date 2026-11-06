package com.newlecture.resuit.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	Page<Post> findByTypeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(String type, Pageable pageable);

	Page<Post> findByUser_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(
			List<UUID> userIds,
			Pageable pageable);

	Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}