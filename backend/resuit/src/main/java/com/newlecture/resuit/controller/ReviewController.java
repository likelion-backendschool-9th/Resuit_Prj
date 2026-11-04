package com.newlecture.resuit.controller;

import java.security.Principal;
import com.newlecture.resuit.dto.ReviewAcceptResponse;
import com.newlecture.resuit.dto.HallOfFameQuery;
import com.newlecture.resuit.dto.HallOfFameResponse;
import com.newlecture.resuit.dto.BestReviewsResponse;
import com.newlecture.resuit.dto.ReviewLikeCommand;
import com.newlecture.resuit.dto.ReviewLikeResponse;
import com.newlecture.resuit.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

	@GetMapping("/best")
	public ResponseEntity<BestReviewsResponse> getBestReviews(
            @RequestParam(required = false) String period, Principal principal) {
        return ResponseEntity.ok(reviewService.getBestReviews(period, principal == null ? null : principal.getName()));
	}

	@PostMapping("/{id}/likes")
	public ResponseEntity<ReviewLikeResponse> likeReview(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(reviewService.setLiked(new ReviewLikeCommand(id, principal == null ? null : principal.getName(), true)));
	}

	@DeleteMapping("/{id}/likes")
	public ResponseEntity<ReviewLikeResponse> unlikeReview(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(reviewService.setLiked(new ReviewLikeCommand(id, principal == null ? null : principal.getName(), false)));
	}

	@PostMapping("/{reviewId}/accept")
	public ResponseEntity<ReviewAcceptResponse> acceptReview(@PathVariable Long reviewId, Principal principal) {
        return ResponseEntity.ok(reviewService.acceptReview(reviewId, principal == null ? null : principal.getName()));
	}

	@GetMapping("/hall-of-fame")
	public ResponseEntity<HallOfFameResponse> getHallOfFame(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Principal principal) {
        return ResponseEntity.ok(reviewService.getHallOfFame(
            new HallOfFameQuery(page, size, principal == null ? null : principal.getName())));
	}
    public record ReviewErrorResponse(String code, String message) {}

    @ExceptionHandler(ReviewService.UnauthenticatedUserException.class)
    public ResponseEntity<ReviewErrorResponse> unauthenticated() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ReviewErrorResponse("UNAUTHENTICATED", "로그인이 필요합니다."));
    }

    @ExceptionHandler(ReviewService.InvalidReviewIdException.class)
    public ResponseEntity<ReviewErrorResponse> invalidReviewId() {
        return ResponseEntity.badRequest()
            .body(new ReviewErrorResponse("INVALID_REVIEW_ID", "리뷰 ID는 양수여야 합니다."));
    }

    @ExceptionHandler(ReviewService.ReviewNotFoundException.class)
    public ResponseEntity<ReviewErrorResponse> reviewNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ReviewErrorResponse("REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."));
    }
    @ExceptionHandler(ReviewService.InvalidPeriodException.class)
    public ResponseEntity<ReviewErrorResponse> invalidPeriod() {
        return ResponseEntity.badRequest()
            .body(new ReviewErrorResponse("INVALID_PERIOD", "기간은 WEEKLY 또는 MONTHLY여야 합니다."));
    }
    @ExceptionHandler(ReviewService.InvalidPaginationException.class)
    public ResponseEntity<ReviewErrorResponse> invalidPagination() {
        return ResponseEntity.badRequest()
            .body(new ReviewErrorResponse("INVALID_PAGINATION", "page는 0~100000, size는 1~50이어야 합니다."));
    }
    @ExceptionHandler(ReviewService.ReviewForbiddenException.class)
    public ResponseEntity<ReviewErrorResponse> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ReviewErrorResponse("REVIEW_FORBIDDEN", "피드백 요청자만 채택할 수 있습니다."));
    }

    @ExceptionHandler(ReviewService.RequestClosedException.class)
    public ResponseEntity<ReviewErrorResponse> requestClosed() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ReviewErrorResponse("REQUEST_CLOSED", "취소한 피드백 요청에서는 채택할 수 없습니다."));
    }

    @ExceptionHandler(ReviewService.ReviewAlreadyAcceptedException.class)
    public ResponseEntity<ReviewErrorResponse> alreadyAccepted() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ReviewErrorResponse("REVIEW_ALREADY_ACCEPTED", "이미 다른 리뷰를 채택했습니다."));
    }
}
