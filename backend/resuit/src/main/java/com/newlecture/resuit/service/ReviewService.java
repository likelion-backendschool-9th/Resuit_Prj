package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.BestReviewsResponse;
import com.newlecture.resuit.dto.ReviewHighlightsResponse;
import com.newlecture.resuit.dto.HallOfFameQuery;
import com.newlecture.resuit.dto.HallOfFameResponse;
import com.newlecture.resuit.dto.ReviewAcceptResponse;
import com.newlecture.resuit.dto.ReviewLikeCommand;
import com.newlecture.resuit.dto.ReviewLikeResponse;

public interface ReviewService {
    ReviewHighlightsResponse getHighlights();
    BestReviewsResponse getBestReviews(String period, String username);
    HallOfFameResponse getHallOfFame(HallOfFameQuery query);
    ReviewLikeResponse setLiked(ReviewLikeCommand command);
    ReviewAcceptResponse acceptReview(Long reviewId, String username);

    public static class InvalidPaginationException extends RuntimeException {}
    public static class InvalidPeriodException extends RuntimeException {}
    public static class ReviewForbiddenException extends RuntimeException {}
    public static class RequestClosedException extends RuntimeException {}
    public static class ReviewAlreadyAcceptedException extends RuntimeException {}

    public static class UnauthenticatedUserException extends RuntimeException {}
    public static class InvalidReviewIdException extends RuntimeException {}
    public static class ReviewNotFoundException extends RuntimeException {}
}
