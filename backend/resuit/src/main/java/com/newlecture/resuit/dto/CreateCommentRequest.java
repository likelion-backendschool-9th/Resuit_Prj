package com.newlecture.resuit.dto;

import java.util.UUID;

public record CreateCommentRequest(
		UUID userId,
		String content,
		Long parentId) {
}
