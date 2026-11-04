package com.newlecture.resuit.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.DeleteCommentRequest;
import com.newlecture.resuit.dto.UpdateCommentRequest;
import com.newlecture.resuit.entity.Comment;
import com.newlecture.resuit.repository.CommentRepository;
import com.newlecture.resuit.repository.PostRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommentService;

@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public CommentServiceImpl(
			CommentRepository commentRepository,
			PostRepository postRepository,
			UserRepository userRepository) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
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

	@Override
	@Transactional
	public CommentResponse updateComment(Long commentId, UpdateCommentRequest request) {
		var comment = findComment(commentId);
		validateOwner(comment, request.userId());
		comment.updateContent(request.content());
		return CommentResponse.from(comment, List.of());
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId, DeleteCommentRequest request) {
		var comment = findComment(commentId);
		validateOwner(comment, request.userId());
		comment.delete();
	}

	private com.newlecture.resuit.entity.Post findPost(Long id) {
		return postRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
	}

	private Comment findComment(Long id) {
		return commentRepository.findById(id)
				.filter(comment -> comment.getDeletedAt() == null)
				.orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + id));
	}

	private void validateOwner(Comment comment, java.util.UUID userId) {
		if (!Objects.equals(comment.getUser().getId(), userId)) {
			throw new IllegalArgumentException("댓글을 수정하거나 삭제할 권한이 없습니다.");
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
