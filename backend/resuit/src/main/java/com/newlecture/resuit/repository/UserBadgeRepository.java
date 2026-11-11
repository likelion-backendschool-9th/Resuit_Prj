package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.entity.UserBadgeId;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    Optional<UserBadge> findByIdUserIdAndIdBadgeId(UUID userId, Integer badgeId);
    List<UserBadge> findByIdUserId(UUID userId);
}
