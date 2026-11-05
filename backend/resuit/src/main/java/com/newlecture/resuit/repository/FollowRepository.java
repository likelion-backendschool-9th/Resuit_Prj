package com.newlecture.resuit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Follow;
import com.newlecture.resuit.entity.FollowId;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

	List<Follow> findByIdFollowerId(UUID followerId);
}
