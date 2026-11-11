package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record BlogPostResponse(Long id, String title, String description, String url,
        LocalDateTime publishedAt) {
}
