package com.newlecture.resuit.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByImageUrlInAndUser_IdAndDeletedAtIsNull(java.util.Collection<String> paths, UUID ownerId);


	Page<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	Page<Post> findByTypeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(String type, Pageable pageable);

	Page<Post> findByUser_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(
			List<UUID> userIds,
			Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
            select p from Post p where upper(p.type) = 'TECH' and p.deletedAt is null
            and :tag member of p.tags order by p.createdAt desc, p.id desc
            """)
    Page<Post> findTechFeedByTag(@org.springframework.data.repository.query.Param("tag") String tag, Pageable pageable);

	Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}