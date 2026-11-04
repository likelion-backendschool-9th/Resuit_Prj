package com.newlecture.resuit.dto;

import java.time.LocalDate;
import java.util.List;

public record BestReviewsResponse(String period, List<Review> reviews) {
    public record Review(Long reviewId, String username, String nickname, String content,
                         String section, boolean adopted, LocalDate periodStart,
                         int rank, long likeCount, boolean liked, boolean canAccept) {}
}
