package com.newlecture.resuit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

	@GetMapping("/best")
	public ResponseEntity<Void> getBestReviews(
			@RequestParam(required = false) String period) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/likes")
	public ResponseEntity<Void> likeReview(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}/likes")
	public ResponseEntity<Void> unlikeReview(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{reviewId}/accept")
	public ResponseEntity<Void> acceptReview(@PathVariable Long reviewId) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/hall-of-fame")
	public ResponseEntity<Void> getHallOfFame() {
		return ResponseEntity.ok().build();
	}
}
