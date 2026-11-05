package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Post;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
    Page<Post> findByUserIdAndTypeAndDeletedAtIsNull(UUID userId, String type, Pageable pageable);
    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
