package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.service.PostService;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;

	public PostServiceImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public Page<PostTimelineResponse> getTimeline(Pageable pageable) {
		return postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
				.map(PostTimelineResponse::from);
	}
}
