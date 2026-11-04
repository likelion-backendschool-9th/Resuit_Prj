package com.newlecture.resuit.dto;

public record UpdatePostRequest(
		String type,
		String content,
		String imageUrl,
		String videoUrl,
		String repoFullName) {
}
