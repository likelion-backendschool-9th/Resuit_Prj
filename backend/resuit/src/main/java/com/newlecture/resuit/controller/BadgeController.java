package com.newlecture.resuit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.newlecture.resuit.dto.ReviewerProgressResponse;
import com.newlecture.resuit.service.BadgeProgressService;
import com.newlecture.resuit.service.CurrentUserResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BadgeController {
	private final BadgeProgressService progress;
	private final CurrentUserResolver currentUser;

	public BadgeController(BadgeProgressService progress, CurrentUserResolver currentUser) {
		this.progress = progress;
		this.currentUser = currentUser;
	}

	@GetMapping("/badges/me/reviewer-progress")
	public ReviewerProgressResponse getReviewerProgress(
			@RequestHeader(value = "X-Username", required = false) String username) {
		return progress.getProgress(currentUser.requireUserId(username));
	}

	@GetMapping("/verifications/me")
	public ResponseEntity<Void> getMyVerificationStatus() {
		return ResponseEntity.ok().build();
	}
}
