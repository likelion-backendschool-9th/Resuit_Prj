package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.DeletePostRequest;
import com.newlecture.resuit.dto.UpdatePostRequest;

public interface PostService {

	Page<PostTimelineResponse> getTimeline(Pageable pageable);

	PostDetailResponse getPostDetail(Long id);

	PostDetailResponse createPost(CreatePostRequest request);

	PostDetailResponse updatePost(Long id, UpdatePostRequest request);

	void deletePost(Long id, DeletePostRequest request);
}