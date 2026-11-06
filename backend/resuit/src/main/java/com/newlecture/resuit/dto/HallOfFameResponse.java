package com.newlecture.resuit.dto;

import java.time.LocalDate;
import java.util.List;

public record HallOfFameResponse(List<Reviewer> reviewers, int page, int size, long totalElements, boolean hasNext) {
    public record Reviewer(long rank, String username, String nickname, long reviewCount,
                           long selectionCount, long weeklyCount, long monthlyCount,
                           Long representativeReviewId, String representativeContent, LocalDate latestSelection) {}
}
