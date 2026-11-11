package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.Post;

public record PostTimelineResponse(
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

	public static PostTimelineResponse from(Post post) {
		return from(post, 0, 0, false);
	}

	public static PostTimelineResponse from(Post post, long commentCount, long likeCount, boolean liked) {
		var tech = post.getTechTags();
		var hash = post.hashtagValues();
		var user = post.getUser();
		var badge = user.getDisplayedBadge();
		return new PostTimelineResponse(
				post.getId(),
				new Author(
						user.getUsername(),
						user.getNickname(),
						user.getProfileImageUrl(),
						badge == null ? null : badge.getName()),
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
			String profileImageUrl,
			String displayedBadgeName) {
	}
}
