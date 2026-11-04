package com.newlecture.resuit.dto;

public record CreateCommentRequest(
		String content,
		Long parentId) {
}
