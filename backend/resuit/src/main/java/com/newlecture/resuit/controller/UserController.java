package com.newlecture.resuit.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 뭔가 꼬인거 같으니 대충 추가해서 pr을 최신화
@RestController
@RequestMapping ("/api/v1/users")
public class UserController {

	@GetMapping("/me")
	public ResponseEntity<Void> getMyProfile() {
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/me")
	public ResponseEntity<Void> updateMyProfile(
			@RequestBody(required = false) Map<String, Object> profileRequest) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{username}")
	public ResponseEntity<Void> getUserProfile(@PathVariable String username) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{username}/tweets")
	public ResponseEntity<Void> getUserTweets(
			@PathVariable String username,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{username}/feedbacks")
	public ResponseEntity<Void> getUserFeedbacks(
			@PathVariable String username,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok().build();
	}
}
