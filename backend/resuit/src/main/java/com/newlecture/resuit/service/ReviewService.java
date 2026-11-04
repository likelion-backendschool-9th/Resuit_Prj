package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.ReviewLikeResponse;
import com.newlecture.resuit.dto.BestReviewsResponse;
import com.newlecture.resuit.repository.BestReviewRepository;
import com.newlecture.resuit.dto.HallOfFameQuery;
import com.newlecture.resuit.dto.HallOfFameResponse;
import org.springframework.data.domain.PageRequest;
import com.newlecture.resuit.repository.FeedbackRequestRepository;
import com.newlecture.resuit.dto.ReviewAcceptResponse;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import com.newlecture.resuit.dto.ReviewLikeCommand;
import com.newlecture.resuit.repository.FeedbackCommentLikeRepository;
import com.newlecture.resuit.repository.FeedbackCommentRepository;
import com.newlecture.resuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final FeedbackCommentRepository comments;
    private final FeedbackCommentLikeRepository likes;
    private final UserRepository users;
    private final BestReviewRepository bestReviews;
    private final FeedbackRequestRepository requests;

    @Transactional(readOnly = true)
    public BestReviewsResponse getBestReviews(String period, String username) {
        String normalized = period == null ? "weekly" : period.toLowerCase(Locale.ROOT);
        if (!normalized.equals("weekly") && !normalized.equals("monthly")) {
            throw new InvalidPeriodException();
        }
        var reviews = bestReviews.findLatest(normalized, viewerId(username)).stream()
            .map(this::toReview).toList();
        return new BestReviewsResponse(normalized.toUpperCase(Locale.ROOT), reviews);
    }

    @Transactional(readOnly = true)
    public HallOfFameResponse getHallOfFame(HallOfFameQuery query) {
        if (query.page() < 0 || query.page() > 100000 || query.size() < 1 || query.size() > 50) {
            throw new InvalidPaginationException();
        }
        var history = bestReviews.findHistory(viewerId(query.username()), PageRequest.of(query.page(), query.size()));
        var entries = history.getContent().stream().map(row ->
            new HallOfFameResponse.Entry(row.getSelectionId(), row.getPeriod(), toReview(row))).toList();
        return new HallOfFameResponse(entries, query.page(), query.size(), history.getTotalElements(), history.hasNext());
    }

    private UUID viewerId(String username) {
        return username == null ? new UUID(0, 0)
            : users.findByUsername(username).map(user -> user.getId()).orElse(new UUID(0, 0));
    }

    private BestReviewsResponse.Review toReview(BestReviewRepository.ReviewRow row) {
        return new BestReviewsResponse.Review(row.getReviewId(), row.getUsername(), row.getNickname(),
            row.getContent(), row.getSection(), row.getAdopted(), row.getPeriodStart(),
            row.getRank(), row.getLikeCount(), row.getLiked(), row.getCanAccept());
    }

    public static class InvalidPaginationException extends RuntimeException {}

    public static class InvalidPeriodException extends RuntimeException {}

    @Transactional
    public ReviewLikeResponse setLiked(ReviewLikeCommand command) {
        var reviewId = command.reviewId();
        var username = command.username();
        var liked = command.liked();
        if (username == null || username.isBlank()) {
            throw new UnauthenticatedUserException();
        }
        var user = users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
        if (reviewId == null || reviewId <= 0) {
            throw new InvalidReviewIdException();
        }
        if (!comments.existsById(reviewId)) {
            throw new ReviewNotFoundException();
        }
        if (liked) {
            likes.insertIfAbsent(reviewId, user.getId());
        } else {
            likes.deleteLike(reviewId, user.getId());
        }
        return new ReviewLikeResponse(reviewId, liked);
    }

    @Transactional
    public ReviewAcceptResponse acceptReview(Long reviewId, String username) {
        if (username == null || username.isBlank()) throw new UnauthenticatedUserException();
        var user = users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
        if (reviewId == null || reviewId <= 0) throw new InvalidReviewIdException();
        var requestId = comments.findRequestId(reviewId).orElseThrow(ReviewNotFoundException::new);
        // Serialize all acceptance attempts for the same feedback request.
        var request = requests.findForAcceptance(requestId).orElseThrow(ReviewNotFoundException::new);
        if (!request.getUser().getId().equals(user.getId())) throw new ReviewForbiddenException();
        if (request.getDeletedAt() != null) throw new RequestClosedException();
        var review = comments.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        if (!review.isAdopted()) {
            if (comments.existsByFeedbackRequestIdAndIsAdoptedTrue(requestId)) {
                throw new ReviewAlreadyAcceptedException();
            }
            review.accept(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        }
        return new ReviewAcceptResponse(reviewId, review.isAdopted(), review.getAdoptedAt());
    }

    public static class ReviewForbiddenException extends RuntimeException {}
    public static class RequestClosedException extends RuntimeException {}
    public static class ReviewAlreadyAcceptedException extends RuntimeException {}

    public static class UnauthenticatedUserException extends RuntimeException {}
    public static class InvalidReviewIdException extends RuntimeException {}
    public static class ReviewNotFoundException extends RuntimeException {}
}
