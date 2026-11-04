package com.newlecture.resuit.dto;

import java.util.List;

public record HallOfFameResponse(List<Entry> reviews, int page, int size, long totalElements, boolean hasNext) {
    public record Entry(Long selectionId, String period, BestReviewsResponse.Review review) {}
}
