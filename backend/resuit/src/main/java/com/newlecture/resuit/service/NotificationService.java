package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.notification.NotificationCommand;
import com.newlecture.resuit.dto.notification.NotificationCountResponse;
import com.newlecture.resuit.dto.notification.NotificationResponse;
import com.newlecture.resuit.entity.User;

public interface NotificationService {

	// 알림 문장에 쓸 회원 이름. 닉네임이 없으면 아이디를 쓴다.
	static String nameOf(User user) {
		return user.getNickname() == null || user.getNickname().isBlank() ? user.getUsername() : user.getNickname();
	}

	// 알림을 남긴다. 다른 서비스의 트랜잭션 안에서 부른다. 자기 자신에게는 남기지 않는다.
	void notify(NotificationCommand command);

	// 내 알림 목록(최신순)
	Page<NotificationResponse> getMine(String username, Pageable pageable);

	// 내 안 읽은 알림 수
	NotificationCountResponse countUnread(String username);

	// 알림 하나를 읽음으로 바꾼다.
	NotificationResponse read(Long id, String username);

	// 내 알림을 모두 읽음으로 바꾼다.
	NotificationCountResponse readAll(String username);
}
