package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record ProfilePostResponse(Long id, String type, String content,
        String imageUrl, String videoUrl, LocalDateTime createdAt,
        long commentCount, long likeCount, boolean liked) {
}
