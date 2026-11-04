package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;

public interface PostService {

	Page<PostTimelineResponse> getTimeline(Pageable pageable);

	PostDetailResponse getPostDetail(Long id);
}