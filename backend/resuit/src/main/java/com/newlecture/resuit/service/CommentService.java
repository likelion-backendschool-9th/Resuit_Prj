package com.newlecture.resuit.service;

import java.util.List;

import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.CreateCommentRequest;

public interface CommentService {

	List<CommentResponse> getComments(Long postId);

	CommentResponse createComment(Long postId, CreateCommentRequest request);
}
