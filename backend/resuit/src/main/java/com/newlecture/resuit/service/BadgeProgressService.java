package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.ReviewerProgressResponse;
import java.util.UUID;

public interface BadgeProgressService {
    ReviewerProgressResponse getProgress(UUID userId);
}
