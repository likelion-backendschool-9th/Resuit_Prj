package com.newlecture.resuit.service;

import java.util.List;

import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.UpdateCommentRequest;

public interface CommentService {

	List<CommentResponse> getComments(Long postId);

	CommentResponse createComment(Long postId, CreateCommentRequest request, String username);

	CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String username);

	void deleteComment(Long commentId, String username);
}
