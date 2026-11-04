package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.DeletePostRequest;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostLikeRequest;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.entity.Post;
import com.newlecture.resuit.entity.PostLike;
import com.newlecture.resuit.entity.PostLikeId;
import com.newlecture.resuit.repository.PostLikeRepository;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.PostService;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;

	public PostServiceImpl(
			PostRepository postRepository,
			UserRepository userRepository,
			PostLikeRepository postLikeRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.postLikeRepository = postLikeRepository;
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

	@Override
	@Transactional
	public PostDetailResponse updatePost(Long id, UpdatePostRequest request) {
		var post = findPost(id);
		validateOwner(post, request.userId());
		post.update(
				request.type(),
				request.content(),
				request.imageUrl(),
				request.videoUrl(),
				request.repoFullName());
		return PostDetailResponse.from(post);
	}

	@Override
	@Transactional
	public void deletePost(Long id, DeletePostRequest request) {
		var post = findPost(id);
		validateOwner(post, request.userId());
		post.delete();
	}

	@Override
	@Transactional
	public void likePost(Long postId, PostLikeRequest request) {
		var post = findPost(postId);
		var user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userId()));
				
		var likeId = new PostLikeId(post.getId(), user.getId());
		if (!postLikeRepository.existsById(likeId)) {
			postLikeRepository.save(new PostLike(post, user));
		}
	}

	@Override
	@Transactional
	public void unlikePost(Long postId, PostLikeRequest request) {
		var post = findPost(postId);
		var likeId = new PostLikeId(post.getId(), request.userId());
		postLikeRepository.deleteById(likeId);
	}

	/*
	findPost 사용처
	- updatePost
	- deletePost
	*/
	private Post findPost(Long id) {
		return postRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
	}

	/*
	validateOwner 사용처
	- updatePost
	- deletePost
	*/
	private void validateOwner(Post post, java.util.UUID userId) {
		if (!Objects.equals(post.getUser().getId(), userId)) {
			throw new IllegalArgumentException("게시글을 수정하거나 삭제할 권한이 없습니다.");
		}
	}

}