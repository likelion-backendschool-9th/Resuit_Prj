package com.newlecture.resuit.dto;

public record CreatePostRequest(
		String type,
		String content,
		String imageUrl,
		String videoUrl,
		String repoFullName,
        @jakarta.validation.constraints.Size(max = 3) java.util.List<String> tags) {
}
