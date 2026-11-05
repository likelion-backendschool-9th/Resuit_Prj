package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.newlecture.resuit.entity.Comment;

public record CommentResponse(
		Long id,
		Author author,
		String content,
		LocalDateTime createdAt,
		List<CommentResponse> replies) {

	public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
		return new CommentResponse(
				comment.getId(),
				new Author(
						comment.getUser().getUsername(),
						comment.getUser().getNickname(),
						comment.getUser().getProfileImageUrl()),
				comment.getContent(),
				comment.getCreatedAt(),
				replies);
	}

	public record Author(
			String username,
			String nickname,
			String profileImageUrl) {
	}
}
