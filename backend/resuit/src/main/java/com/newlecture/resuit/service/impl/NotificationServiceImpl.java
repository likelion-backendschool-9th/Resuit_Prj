package com.newlecture.resuit.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.notification.NotificationCommand;
import com.newlecture.resuit.dto.notification.NotificationCountResponse;
import com.newlecture.resuit.dto.notification.NotificationResponse;
import com.newlecture.resuit.entity.Notification;
import com.newlecture.resuit.repository.NotificationRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.NotificationService;

// 알림을 남기고, 내 알림을 보여 주고, 읽음으로 바꾼다.
@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

	// 알림 문장 최대 길이(notifications.message)
	private static final int MAX_MESSAGE = 200;

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void notify(NotificationCommand command) {
		// 내가 한 일은 나에게 알리지 않는다. (내 글에 내가 댓글을 단 경우 등)
		if (command.userId().equals(command.actorId())) {
			return;
		}
		String message = command.message().length() > MAX_MESSAGE
				? command.message().substring(0, MAX_MESSAGE - 1) + "…"
				: command.message();
		notificationRepository.save(Notification.builder()
				// 회원은 이미 있는 행이라 다시 조회하지 않고 참조만 건다.
				.user(userRepository.getReferenceById(command.userId()))
				.actor(command.actorId() == null ? null : userRepository.getReferenceById(command.actorId()))
				.type(command.type().name())
				.message(message)
				.link(command.link())
				.build());
	}

	@Override
	public Page<NotificationResponse> getMine(String username, Pageable pageable) {
		return notificationRepository.findByUser_IdOrderByIdDesc(findUserId(username), pageable)
				.map(NotificationResponse::from);
	}

	@Override
	public NotificationCountResponse countUnread(String username) {
		return new NotificationCountResponse(notificationRepository.countByUser_IdAndReadAtIsNull(findUserId(username)));
	}

	@Override
	@Transactional
	public NotificationResponse read(Long id, String username) {
		// 남의 알림이면 없는 알림처럼 404다.
		Notification notification = notificationRepository.findByIdAndUser_Id(id, findUserId(username))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
		notification.read(LocalDateTime.now());
		return NotificationResponse.from(notification);
	}

	@Override
	@Transactional
	public NotificationCountResponse readAll(String username) {
		UUID userId = findUserId(username);
		notificationRepository.readAll(userId, LocalDateTime.now());
		return new NotificationCountResponse(0);
	}

	// 로그인한 username으로 회원 ID를 찾는다. 세션은 있는데 회원이 없으면 401이다.
	private UUID findUserId(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
				.getId();
	}
}
