package com.newlecture.resuit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/v1/posts")
public class PostsController {

	@PostMapping("/")
	public ResponseEntity<Void> createPost() {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Void> updatePost(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Void> getPost(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<Void> createComment(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{id}/comments")
	public ResponseEntity<Void> updateComment(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}/comments")
	public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}/comments")
	public ResponseEntity<Void> getComments(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/likes")
	public ResponseEntity<Void> likePost(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}/likes")
	public ResponseEntity<Void> unlikePost(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	/*
	* 피드 및 이미지, 영상 업로드쪽은 아직 어떻게 할지 정해지지않아. posts와 일단 같이 관리
	*/
	@GetMapping(value = "/feeds", params = "tab=recommend")
	public ResponseEntity<Void> getRecommendedFeed() {
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/feeds", params = "tab=following")
	public ResponseEntity<Void> getFollowingFeed() {
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/feeds", params = "tab=tech")
	public ResponseEntity<Void> getTechFeed() {
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/feeds", params = "tab=question")
	public ResponseEntity<Void> getQuestionFeed() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/uploads")
	public ResponseEntity<Void> uploadMedia() {
		return ResponseEntity.ok().build();
	}
}
