package com.newlecture.resuit.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CreatePostRequest;
import com.newlecture.resuit.dto.CreateCommentRequest;
import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.dto.UpdateCommentRequest;
import com.newlecture.resuit.dto.MediaUploadResponse;
import com.newlecture.resuit.service.CommentService;
import com.newlecture.resuit.service.MediaService;
import com.newlecture.resuit.service.PostService;

@RestController
@RequestMapping ("/api/v1/posts")
public class PostsController {

	private final PostService postService;
	private final CommentService commentService;
	private final MediaService mediaService;

	public PostsController(PostService postService, CommentService commentService, MediaService mediaService) {
		this.postService = postService;
		this.commentService = commentService;
		this.mediaService = mediaService;
	}

	@GetMapping
	public Page<PostTimelineResponse> getPost(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getTimeline(PageRequest.of(page, size));
	}

	@PostMapping("/")
	public PostDetailResponse createPost(@RequestBody CreatePostRequest request, Principal principal) {
		return postService.createPost(request, principal.getName());
	}

	@PatchMapping("/{id}")
	public PostDetailResponse updatePost(
			@PathVariable Long id,
			@RequestBody UpdatePostRequest request,
			Principal principal) {
		return postService.updatePost(id, request, principal.getName());
	}

	@DeleteMapping("/{id}")
	public void deletePost(
			@PathVariable Long id,
			Principal principal) {
		postService.deletePost(id, principal.getName());
	}

	@GetMapping("/{id}")
	public PostDetailResponse getPostDetail(@PathVariable Long id) {
		return postService.getPostDetail(id);
	}

	@PostMapping("/{id}/comments")
	public CommentResponse createComment(
			@PathVariable Long id,
			@RequestBody CreateCommentRequest request,
			Principal principal) {
		return commentService.createComment(id, request, principal.getName());
	}

	@PatchMapping("/{id}/comments")
	public CommentResponse updateComment(
			@PathVariable Long id,
			@RequestBody UpdateCommentRequest request,
			Principal principal) {
		return commentService.updateComment(id, request, principal.getName());
	}

	@DeleteMapping("/{id}/comments")
	public void deleteComment(
			@PathVariable Long id,
			Principal principal) {
		commentService.deleteComment(id, principal.getName());
	}

	@GetMapping("/{id}/comments")
	public List<CommentResponse> getComments(@PathVariable Long id) {
		return commentService.getComments(id);
	}

	@PostMapping("/{id}/likes")
	public void likePost(
			@PathVariable Long id,
			Principal principal) {
		postService.likePost(id, principal.getName());
	}

	@DeleteMapping("/{id}/likes")
	public void unlikePost(
			@PathVariable Long id,
			Principal principal) {
		postService.unlikePost(id, principal.getName());
	}

	/*
	* 피드 및 이미지, 영상 업로드쪽은 아직 어떻게 할지 정해지지않아. posts와 일단 같이 관리
	*/
	@GetMapping(value = "/feeds", params = "tab=recommend")
	public Page<PostTimelineResponse> getRecommendedFeed(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getRecommendedFeed(PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=following")
	public Page<PostTimelineResponse> getFollowingFeed(
			@RequestParam UUID userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getFollowingFeed(userId, PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=tech")
	public Page<PostTimelineResponse> getTechFeed(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getTechFeed(PageRequest.of(page, size));
	}

	@GetMapping(value = "/feeds", params = "tab=question")
	public Page<PostTimelineResponse> getQuestionFeed(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getQuestionFeed(PageRequest.of(page, size));
	}

	@PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public MediaUploadResponse uploadMedia(
			@RequestParam("file") MultipartFile file,
			Principal principal) {
		return mediaService.upload(file, principal.getName());
	}

	@DeleteMapping("/uploads")
	public void discardMedia(
			@RequestParam("path") String path,
			Principal principal) {
		mediaService.discard(path, principal.getName());
	}
}
