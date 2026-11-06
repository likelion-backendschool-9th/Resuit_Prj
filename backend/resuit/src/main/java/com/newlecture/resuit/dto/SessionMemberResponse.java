package com.newlecture.resuit.dto;

import java.util.UUID;

public record SessionMemberResponse(UUID id, String username, String nickname) {}
