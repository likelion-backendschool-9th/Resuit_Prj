package com.newlecture.resuit.dto.notification;

import java.util.UUID;

import com.newlecture.resuit.service.NotificationType;

// 알림을 남길 때 다른 서비스가 넘기는 값이다.
public record NotificationCommand(
		// 알림을 받는 회원
		UUID userId,
		// 알림을 일으킨 회원. 없으면 null
		UUID actorId,
		// 알림 종류
		NotificationType type,
		// 화면에 보여 줄 문장
		String message,
		// 누르면 이동할 화면 주소
		String link) {
}
