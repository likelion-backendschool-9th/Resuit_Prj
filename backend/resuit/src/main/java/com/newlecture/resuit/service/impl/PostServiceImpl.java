package com.newlecture.resuit.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.entity.Post;
import com.newlecture.resuit.entity.PostLike;
import com.newlecture.resuit.entity.PostLikeId;
import com.newlecture.resuit.repository.PostLikeRepository;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.repository.FollowRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.PostService;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;

	public PostServiceImpl(
			PostRepository postRepository,
			UserRepository userRepository,
			PostLikeRepository postLikeRepository,
			FollowRepository followRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.postLikeRepository = postLikeRepository;
		this.followRepository = followRepository;
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
	public PostDetailResponse createPost(CreatePostRequest request, String username) {
		var user = findUser(username);

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
	public PostDetailResponse updatePost(Long id, UpdatePostRequest request, String username) {
		var post = findPost(id);
		validateOwner(post, findUser(username).getId());
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
	public void deletePost(Long id, String username) {
		var post = findPost(id);
		validateOwner(post, findUser(username).getId());
		post.delete();
	}

	@Override
	@Transactional
	public void likePost(Long postId, String username) {
		var post = findPost(postId);
		var user = findUser(username);
				
		var likeId = new PostLikeId(post.getId(), user.getId());
		if (!postLikeRepository.existsById(likeId)) {
			postLikeRepository.save(new PostLike(post, user));
		}
	}

	@Override
	@Transactional
	public void unlikePost(Long postId, String username) {
		var post = findPost(postId);
		var likeId = new PostLikeId(post.getId(), findUser(username).getId());
		postLikeRepository.deleteById(likeId);
	}

	@Override
	public Page<PostTimelineResponse> getRecommendedFeed(Pageable pageable) {
		return getTimeline(pageable);
	}

	@Override
	public Page<PostTimelineResponse> getFollowingFeed(UUID userId, Pageable pageable) {
		if (!userRepository.existsById(userId)) {
			throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
		}
		List<UUID> followingIds = followRepository.findByIdFollowerId(userId).stream()
				.map(follow -> follow.getFollowing().getId())
				.toList();
		if (followingIds.isEmpty()) {
			return Page.empty(pageable);
		}
		return postRepository.findByUser_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(
						followingIds, pageable)
				.map(PostTimelineResponse::from);
	}

	@Override
	public Page<PostTimelineResponse> getTechFeed(Pageable pageable) {
		return getFeedByType("TECH", pageable);
	}

	@Override
	public Page<PostTimelineResponse> getQuestionFeed(Pageable pageable) {
		return getFeedByType("QUESTION", pageable);
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

	private com.newlecture.resuit.entity.User findUser(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
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

	private Page<PostTimelineResponse> getFeedByType(String type, Pageable pageable) {
		return postRepository.findByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(type, pageable)
				.map(PostTimelineResponse::from);
	}

}