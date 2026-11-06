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
		LocalDateTime createdAt,
		long commentCount,
		long likeCount,
		boolean liked) {

	public static PostTimelineResponse from(Post post) {
        return from(post, 0, 0, false);
    }

    public static PostTimelineResponse from(Post post, long commentCount, long likeCount, boolean liked) {
		return new PostTimelineResponse(
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
				post.getCreatedAt(), commentCount, likeCount, liked);
	}

	public record Author(
			String username,
			String nickname,
			String profileImageUrl) {
	}
}
