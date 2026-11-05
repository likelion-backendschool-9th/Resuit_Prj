package com.newlecture.resuit.dto;

import java.util.Map;

public record UpdateProfileRequest(String nickname, String selfIntroduction,
        String profileImageUrl, Map<String, Object> widgets) {
}
