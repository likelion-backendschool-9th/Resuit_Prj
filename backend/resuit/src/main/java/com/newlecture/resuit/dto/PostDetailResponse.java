package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.Post;

public record PostDetailResponse(
		Long id,
		Author author,
		String type,
		String content,
		String imageUrl,
		String videoUrl,
		String blogPostUrl,
		String repoFullName,
		java.util.Set<String> techTags,
		java.util.Set<String> hashtags,
		LocalDateTime createdAt,
		long commentCount,
		long likeCount,
		boolean liked) {

	public static PostDetailResponse from(Post post) {
		return from(post, 0, 0, false);
	}

	public static PostDetailResponse from(Post post, long commentCount, long likeCount, boolean liked) {
		var tech = post.getTechTags();
		var hash = post.hashtagValues();
		return new PostDetailResponse(
				post.getId(),
				new Author(
						post.getUser().getUsername(),
						post.getUser().getNickname(),
						post.getUser().getProfileImageUrl()),
				post.getType(),
				post.getContent(),
				post.getImageUrl(),
				post.getVideoUrl(),
				post.getBlogPost() == null ? null : post.getBlogPost().getUrl(),
				post.getRepoFullName(),
				tech.isEmpty() ? null : new java.util.LinkedHashSet<>(tech),
				hash.isEmpty() ? null : hash,
				post.getCreatedAt(), commentCount, likeCount, liked);
	}

	public record Author(
			String username,
			String nickname,
			String profileImageUrl) {
	}
}
