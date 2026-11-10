package com.newlecture.resuit.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.UpdatePostRequest;

public interface PostService {

	Page<PostTimelineResponse> getTimeline(Pageable pageable);

	PostDetailResponse getPostDetail(Long id);

	PostDetailResponse createPost(CreatePostRequest request, String username);

	PostDetailResponse updatePost(Long id, UpdatePostRequest request, String username);

	void deletePost(Long id, String username);

	void likePost(Long postId, String username);

	void unlikePost(Long postId, String username);

	Page<PostTimelineResponse> getRecommendedFeed(Pageable pageable);

	Page<PostTimelineResponse> getFollowingFeed(UUID userId, Pageable pageable);

	Page<PostTimelineResponse> getTechFeed(String tag, Pageable pageable);

	Page<PostTimelineResponse> getQuestionFeed(Pageable pageable);

	Page<PostTimelineResponse> getHashtagFeed(String tag, Pageable pageable);
}