package com.newlecture.resuit.dto;

import java.time.Instant;

public record BlogVerifyStartResponse(
        String code,
        Instant expiresAt,
        String platform,
        String externalId,
        String rssUrl,
        String hint) {
}
