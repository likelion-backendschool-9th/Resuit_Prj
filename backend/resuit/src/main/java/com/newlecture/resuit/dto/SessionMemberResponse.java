package com.newlecture.resuit.dto;

import java.util.UUID;

// role: USER, COMPANY, ADMIN. 화면이 좌측 메뉴를 역할에 맞게 보여 줄 때 쓴다.
public record SessionMemberResponse(UUID id, String username, String nickname, String role) {}
