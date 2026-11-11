package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.UserCareer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCareerRepository extends JpaRepository<UserCareer, Long> {
    List<UserCareer> findByUser_IdOrderByDisplayOrderAsc(UUID userId);
}
