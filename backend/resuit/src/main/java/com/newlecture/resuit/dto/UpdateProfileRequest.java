package com.newlecture.resuit.dto;

import java.util.List;
import java.util.Map;

import com.newlecture.resuit.entity.Region;

// region: Region enum 이름(SEOUL 등). 목록에 없는 값이면 400이고, 값이 없으면 기존 지역을 유지한다.
public record UpdateProfileRequest(String nickname, String selfIntroduction,
        String profileImageUrl, Map<String, Object> widgets, List<CareerRequest> careers,
        Region region) {
}
