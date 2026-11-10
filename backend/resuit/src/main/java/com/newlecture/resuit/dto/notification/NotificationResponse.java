package com.newlecture.resuit.dto.notification;

import java.time.LocalDateTime;

import com.newlecture.resuit.dto.UserSummary;
import com.newlecture.resuit.entity.Notification;

// 알림 목록의 한 줄이다.
public record NotificationResponse(
		// 알림 ID
		Long id,
		// 알림 종류
		String type,
		// 화면에 보여 줄 문장
		String message,
		// 누르면 이동할 화면 주소
		String link,
		// 알림을 일으킨 회원. 없으면 null
		UserSummary actor,
		// 읽었는지
		boolean read,
		// 알림이 생긴 시각
		LocalDateTime createdAt) {

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getMessage(),
				notification.getLink(),
				notification.getActor() == null ? null : UserSummary.from(notification.getActor()),
				notification.getReadAt() != null,
				notification.getCreatedAt());
	}
}
