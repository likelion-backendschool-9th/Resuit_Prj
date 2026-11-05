package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.BlogPost;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByUserIdOrderByPublishedAtDesc(UUID userId, Pageable pageable);
    boolean existsByIntegrationIdAndGuid(Long integrationId, String guid);
}
