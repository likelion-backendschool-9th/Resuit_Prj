package com.newlecture.resuit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.PostLike;
import com.newlecture.resuit.entity.PostLikeId;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
}
