package com.newlecture.resuit.dto;

import java.util.List;

public record AuthAccountResponse(SessionMemberResponse user, List<String> linkedProviders,
        boolean passwordLoginEnabled) {}
