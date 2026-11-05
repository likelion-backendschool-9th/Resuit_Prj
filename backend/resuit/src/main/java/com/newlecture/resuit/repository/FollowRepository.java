package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Follow;
import com.newlecture.resuit.entity.FollowId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    long countByFollowerId(UUID followerId);
    long countByFollowingId(UUID followingId);
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
