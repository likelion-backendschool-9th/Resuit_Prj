package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record IntegrationResponse(String provider, String platform, String externalId,
        boolean connected, LocalDateTime lastSyncedAt) {
}
