package com.newlecture.resuit.dto.feedback;

public record FeedbackCommentCreateCommand(Long requestId, String username, FeedbackCommentCreateRequest request) {}
