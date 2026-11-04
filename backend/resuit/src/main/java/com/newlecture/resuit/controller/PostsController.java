package com.newlecture.resuit.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.newlecture.resuit.dto.CommentResponse;
import com.newlecture.resuit.dto.DeletePostRequest;
import com.newlecture.resuit.dto.PostDetailResponse;
import com.newlecture.resuit.dto.PostTimelineResponse;
import com.newlecture.resuit.dto.UpdatePostRequest;
import com.newlecture.resuit.service.PostService;

@RestController
@RequestMapping ("/api/v1/posts")
public class PostsController {

	private final PostService postService;

	public PostsController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public Page<PostTimelineResponse> getPost(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.getTimeline(PageRequest.of(page, size));
	}

	@PostMapping("/")
	public PostDetailResponse createPost(@RequestBody CreatePostRequest request) {
		return postService.createPost(request);
	}

	@PatchMapping("/{id}")
	public PostDetailResponse updatePost(
			@PathVariable Long id,
			@RequestBody UpdatePostRequest request) {
		return postService.updatePost(id, request);
	}

	@DeleteMapping("/{id}")
	public void deletePost(
			@PathVariable Long id,
			@RequestBody DeletePostRequest request) {
		postService.deletePost(id, request);
	}

	@GetMapping("/{id}")
	public PostDetailResponse getPostDetail(@PathVariable Long id) {
		return postService.getPostDetail(id);
	}

	@PostMapping("/{id}/comments")
	public void createComment(@PathVariable Long id) {
	}

	@PatchMapping("/{id}/comments")
	public void updateComment(@PathVariable Long id) {
	}

	@DeleteMapping("/{id}/comments")
	public void deleteComment(@PathVariable Long id) {
	}

	@GetMapping("/{id}/comments")
	public List<CommentResponse> getComments(@PathVariable Long id) {
		return postService.getComments(id);
	}

	@PostMapping("/{id}/likes")
	public void likePost(@PathVariable Long id) {
	}

	@DeleteMapping("/{id}/likes")
	public void unlikePost(@PathVariable Long id) {
	}

	/*
	* 피드 및 이미지, 영상 업로드쪽은 아직 어떻게 할지 정해지지않아. posts와 일단 같이 관리
	*/
	@GetMapping(value = "/feeds", params = "tab=recommend")
	public void getRecommendedFeed() {
	}

	@GetMapping(value = "/feeds", params = "tab=following")
	public void getFollowingFeed() {
	}

	@GetMapping(value = "/feeds", params = "tab=tech")
	public void getTechFeed() {
	}

	@GetMapping(value = "/feeds", params = "tab=question")
	public void getQuestionFeed() {
	}

	@PostMapping("/uploads")
	public void uploadMedia() {
	}
}
