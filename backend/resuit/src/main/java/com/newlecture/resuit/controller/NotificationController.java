package com.newlecture.resuit.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.notification.NotificationCountResponse;
import com.newlecture.resuit.dto.notification.NotificationResponse;
import com.newlecture.resuit.service.NotificationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// 내 알림. 본인만 보므로 /users/me 아래에 둔다.
@RestController
@RequestMapping("/api/v1/users/me/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	// 내 알림 목록: GET /api/v1/users/me/notifications?page=0&size=20
	@GetMapping
	public ResponseEntity<Page<NotificationResponse>> getNotifications(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
			Principal principal) {
		return ResponseEntity.ok(notificationService.getMine(principal.getName(), PageRequest.of(page, size)));
	}

	// 안 읽은 알림 수: GET /api/v1/users/me/notifications/unread-count
	// 모든 화면의 왼쪽 메뉴가 화면을 옮길 때마다 다시 부르므로 목록과 따로 둔다.
	@GetMapping("/unread-count")
	public ResponseEntity<NotificationCountResponse> getUnreadCount(Principal principal) {
		return ResponseEntity.ok(notificationService.countUnread(principal.getName()));
	}

	// 알림 하나 읽음: PATCH /api/v1/users/me/notifications/{id}/read
	@PatchMapping("/{id}/read")
	public ResponseEntity<NotificationResponse> read(@PathVariable Long id, Principal principal) {
		return ResponseEntity.ok(notificationService.read(id, principal.getName()));
	}

	// 모두 읽음: POST /api/v1/users/me/notifications/read-all
	@PostMapping("/read-all")
	public ResponseEntity<NotificationCountResponse> readAll(Principal principal) {
		return ResponseEntity.ok(notificationService.readAll(principal.getName()));
	}
}
