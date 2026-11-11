package com.newlecture.resuit.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// 내 알림을 최신순으로 읽는다. 보낸 회원 정보를 함께 읽어 목록에서 다시 조회하지 않는다.
	@EntityGraph(attributePaths = "actor")
	Page<Notification> findByUser_IdOrderByIdDesc(UUID userId, Pageable pageable);

	// 안 읽은 알림 수
	long countByUser_IdAndReadAtIsNull(UUID userId);

	// 내 알림 한 건. 남의 알림이면 없다.
	Optional<Notification> findByIdAndUser_Id(Long id, UUID userId);

	// 내 안 읽은 알림을 모두 읽음으로 바꾼다.
	@Modifying(clearAutomatically = true)
	@Query("update Notification n set n.readAt = :now where n.user.id = :userId and n.readAt is null")
	int readAll(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
