package com.newlecture.resuit.controller;

import com.newlecture.resuit.dto.CompanyVerificationDto.MyStatusResponse;
import com.newlecture.resuit.dto.ReviewerProgressResponse;
import com.newlecture.resuit.service.BadgeProgressService;
import com.newlecture.resuit.service.CompanyVerificationService;
import com.newlecture.resuit.service.CurrentUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BadgeController {
	private final BadgeProgressService progress;
	private final CompanyVerificationService verifications;
	private final CurrentUserResolver currentUser;

	public BadgeController(BadgeProgressService progress, CompanyVerificationService verifications,
			CurrentUserResolver currentUser) {
		this.progress = progress;
		this.verifications = verifications;
		this.currentUser = currentUser;
	}

	@GetMapping("/badges/me/reviewer-progress")
	public ReviewerProgressResponse getReviewerProgress(
			@RequestHeader(value = "X-Username", required = false) String username) {
		return progress.getProgress(currentUser.requireUserId(username));
	}

	@GetMapping("/verifications/me")
	public MyStatusResponse getMyVerificationStatus(
			@RequestHeader(value = "X-Username", required = false) String username) {
		return verifications.getMyStatus(currentUser.requireUserId(username));
	}
}
