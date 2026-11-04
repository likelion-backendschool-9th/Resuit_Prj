package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.DeletePostRequest;
import com.newlecture.resuit.dto.PostLikeRequest;
import com.newlecture.resuit.dto.UpdatePostRequest;

public interface PostService {

	Page<PostTimelineResponse> getTimeline(Pageable pageable);

	PostDetailResponse getPostDetail(Long id);

	PostDetailResponse createPost(CreatePostRequest request);

	PostDetailResponse updatePost(Long id, UpdatePostRequest request);

	void deletePost(Long id, DeletePostRequest request);

	void likePost(Long postId, PostLikeRequest request);

	void unlikePost(Long postId, PostLikeRequest request);

	Page<PostTimelineResponse> getRecommendedFeed(Pageable pageable);

	Page<PostTimelineResponse> getFollowingFeed(UUID userId, Pageable pageable);

	Page<PostTimelineResponse> getTechFeed(Pageable pageable);

	Page<PostTimelineResponse> getQuestionFeed(Pageable pageable);
}