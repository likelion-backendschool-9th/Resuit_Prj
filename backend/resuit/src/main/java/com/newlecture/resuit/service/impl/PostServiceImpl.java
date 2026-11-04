package com.newlecture.resuit.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.DeletePostRequest;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.entity.Post;
import com.newlecture.resuit.entity.Comment;
import com.newlecture.resuit.repository.CommentRepository;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.PostService;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;

	public PostServiceImpl(
			PostRepository postRepository,
			UserRepository userRepository,
			CommentRepository commentRepository) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.commentRepository = commentRepository;
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
	public List<CommentResponse> getComments(Long postId) {
		findPost(postId);

		List<Comment> comments = commentRepository
				.findByPost_IdAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
		Map<Long, CommentNode> nodes = new LinkedHashMap<>();
		List<CommentNode> roots = new ArrayList<>();

		for (Comment comment : comments) {
			nodes.put(comment.getId(), new CommentNode(comment));
		}
		for (Comment comment : comments) {
			CommentNode node = nodes.get(comment.getId());
			if (comment.getParent() == null || !nodes.containsKey(comment.getParent().getId())) {
				roots.add(node);
			} else {
				nodes.get(comment.getParent().getId()).replies.add(node);
			}
		}
		return roots.stream().map(CommentNode::toResponse).toList();
	}

	@Override
	@Transactional
	public CommentResponse createComment(Long postId, CreateCommentRequest request) {
		var post = findPost(postId);
		var user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userId()));
		Comment parent = null;

		if (request.parentId() != null) {
			parent = commentRepository.findById(request.parentId())
					.filter(comment -> comment.getDeletedAt() == null)
					.orElseThrow(() -> new IllegalArgumentException(
							"부모 댓글을 찾을 수 없습니다: " + request.parentId()));
			if (!Objects.equals(parent.getPost().getId(), post.getId())) {
				throw new IllegalArgumentException("부모 댓글이 해당 게시글에 속하지 않습니다.");
			}
		}

		var comment = Comment.builder()
				.post(post)
				.user(user)
				.parent(parent)
				.content(request.content())
				.build();

		return CommentResponse.from(commentRepository.save(comment), List.of());
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

	private static final class CommentNode {
		private final Comment comment;
		private final List<CommentNode> replies = new ArrayList<>();

		private CommentNode(Comment comment) {
			this.comment = comment;
		}

		private CommentResponse toResponse() {
			return CommentResponse.from(comment, replies.stream().map(CommentNode::toResponse).toList());
		}
	}
}