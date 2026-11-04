package com.newlecture.resuit.dto;

public record CreatePostRequest(
		String type,
		String content,
		String imageUrl,
		String videoUrl,
		String repoFullName) {
}
