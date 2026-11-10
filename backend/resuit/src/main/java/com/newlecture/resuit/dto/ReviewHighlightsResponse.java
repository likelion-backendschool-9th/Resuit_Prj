package com.newlecture.resuit.dto;

import java.util.List;

/** Public sidebar preview with bounded excerpts and no viewer-specific data. */
public record ReviewHighlightsResponse(List<Review> reviews, List<Reviewer> reviewers) {
    public record Review(String username, String nickname, String content, int rank, long likeCount) {}
    public record Reviewer(long rank, String username, String nickname, long reviewCount,
                           long selectionCount, String representativeContent) {}
}
