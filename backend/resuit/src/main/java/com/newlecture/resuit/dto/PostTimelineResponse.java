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
		boolean liked) {

	public static PostTimelineResponse from(Post post) {
        return from(post, false);
    }

    public static PostTimelineResponse from(Post post, boolean liked) {
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
				post.getCreatedAt(), liked);
	}

	public record Author(
			String username,
			String nickname,
			String profileImageUrl) {
	}
}
