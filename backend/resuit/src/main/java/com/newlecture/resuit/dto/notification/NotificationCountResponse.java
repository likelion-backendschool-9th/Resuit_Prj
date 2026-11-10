package com.newlecture.resuit.dto.notification;

// 안 읽은 알림 수. 필드가 1개여도 규칙대로 DTO로 감싼다. 예: {"unread": 3}
public record NotificationCountResponse(long unread) {
}
