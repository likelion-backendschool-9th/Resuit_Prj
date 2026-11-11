package com.newlecture.resuit.dto;

import java.util.UUID;

import com.newlecture.resuit.entity.User;

public record UserSummary(
		UUID id,
		String username,
		String nickname,
		String profileImageUrl) {

	public static UserSummary from(User user) {
		return new UserSummary(
				user.getId(),
				user.getUsername(),
				user.getNickname(),
				user.getProfileImageUrl());
	}
}
