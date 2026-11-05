package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.UserBadgeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeHistoryRepository extends JpaRepository<UserBadgeHistory, Long> {
    List<UserBadgeHistory> findByUserIdOrderByActiveUntilDesc(UUID userId);
}
