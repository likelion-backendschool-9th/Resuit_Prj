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
import com.newlecture.resuit.service.MediaService;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;
	private final MediaService mediaService;

	public PostServiceImpl(
			PostRepository postRepository,
			UserRepository userRepository,
			PostLikeRepository postLikeRepository,
			FollowRepository followRepository,
			MediaService mediaService) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.postLikeRepository = postLikeRepository;
		this.followRepository = followRepository;
		this.mediaService = mediaService;
	}

	@Override
	public Page<PostTimelineResponse> getTimeline(Pageable pageable) {
		return timeline(postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable));
	}

	@Override
	public PostDetailResponse getPostDetail(Long id) {
		return postRepository.findByIdAndDeletedAtIsNull(id)
				.map(this::detail)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
	}

	@Override
	@Transactional
	public PostDetailResponse createPost(CreatePostRequest request, String username) {
		var user = findUser(username);
		var imagePath = mediaService.finalizeForPost(request.imageUrl(), user.getId(), "IMAGE");
		var videoPath = mediaService.finalizeForPost(request.videoUrl(), user.getId(), "VIDEO");

		var post = Post.builder()
				.user(user)
				.type(request.type())
				.content(request.content())
				.imageUrl(imagePath == null ? null : "/media/" + imagePath)
				.videoUrl(videoPath == null ? null : "/media/" + videoPath)
				.repoFullName(request.repoFullName())
				.build();

		return detail(postRepository.save(post));
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
		return detail(post);
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
		return timeline(postRepository.findByUser_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(followingIds, pageable));
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
		return timeline(postRepository.findByTypeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(type, pageable));
	}

    private java.util.Set<Long> likedIds(List<Long> ids) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (ids.isEmpty() || auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return java.util.Set.of();
        }
        return postLikeRepository.findLikedPostIds(auth.getName(), ids);
    }

    private Page<PostTimelineResponse> timeline(Page<Post> posts) {
        var liked = likedIds(posts.getContent().stream().map(Post::getId).toList());
        return posts.map(post -> PostTimelineResponse.from(post, liked.contains(post.getId())));
    }

    private PostDetailResponse detail(Post post) {
        return PostDetailResponse.from(post, likedIds(List.of(post.getId())).contains(post.getId()));
    }

}