package com.newlecture.resuit.dto;

import java.util.UUID;

public record UpdatePostRequest(
		UUID userId,
		String type,
		String content,
		String imageUrl,
		String videoUrl,
		String repoFullName) {
}
