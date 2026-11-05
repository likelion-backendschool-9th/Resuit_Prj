package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Badge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    Optional<Badge> findByCode(String code);
}
