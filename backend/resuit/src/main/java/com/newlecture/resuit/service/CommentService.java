package com.newlecture.resuit.service;

import java.util.List;

import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.DeleteCommentRequest;
import com.newlecture.resuit.dto.UpdateCommentRequest;

public interface CommentService {

	List<CommentResponse> getComments(Long postId);

	CommentResponse createComment(Long postId, CreateCommentRequest request);

	CommentResponse updateComment(Long commentId, UpdateCommentRequest request);

	void deleteComment(Long commentId, DeleteCommentRequest request);
}
