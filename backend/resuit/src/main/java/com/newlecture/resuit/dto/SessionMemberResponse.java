package com.newlecture.resuit.dto;

import java.util.UUID;

// role: USER, COMPANY, ADMIN. 화면이 좌측 메뉴를 역할에 맞게 보여 줄 때 쓴다.
// profileImageUrl: 프로필 편집 후 Composer·설정 등 다른 화면에도 바로 반영하려고 세션에 포함한다.
public record SessionMemberResponse(
        UUID id, String username, String nickname, String role, String profileImageUrl) {}
