package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.entity.Post;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.PostService;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public PostServiceImpl(PostRepository postRepository, UserRepository userRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Page<PostTimelineResponse> getTimeline(Pageable pageable) {
		return postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
				.map(PostTimelineResponse::from);
	}

	@Override
	public PostDetailResponse getPostDetail(Long id) {
		return postRepository.findByIdAndDeletedAtIsNull(id)
				.map(PostDetailResponse::from)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
	}

	@Override
	@Transactional
	public PostDetailResponse createPost(CreatePostRequest request) {
		var user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userId()));

		var post = Post.builder()
				.user(user)
				.type(request.type())
				.content(request.content())
				.imageUrl(request.imageUrl())
				.videoUrl(request.videoUrl())
				.repoFullName(request.repoFullName())
				.build();

		return PostDetailResponse.from(postRepository.save(post));
	}
}