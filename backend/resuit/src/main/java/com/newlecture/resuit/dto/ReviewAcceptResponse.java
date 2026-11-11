package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record ReviewAcceptResponse(Long reviewId, boolean adopted, LocalDateTime adoptedAt) {}
