package com.newlecture.resuit.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// 회원에게 보내는 알림이다. 댓글, 피드백, 채용 제안처럼 다른 회원이 나에게 한 일을 남긴다.
// 보여 줄 문장(message)과 이동할 주소(link)를 만들 때 저장해 두어, 목록을 읽을 때 다른 테이블을 찾지 않는다.
@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notifications_user_id", columnList = "user_id, id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notification extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 알림을 받는 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림을 일으킨 회원(댓글 쓴 사람 등). 없을 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    // 알림 종류: NotificationType 참고
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    // 화면에 보여 줄 문장
    @Column(name = "message", nullable = false, length = 200)
    private String message;

    // 누르면 이동할 화면 주소(/offers 등)
    @Column(name = "link", nullable = false, length = 255)
    private String link;

    // 읽은 시각. 비어 있으면 안 읽은 알림이다.
    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 읽음으로 표시한다. 이미 읽었으면 처음 읽은 시각을 그대로 둔다.
    public void read(LocalDateTime now) {
        if (this.readAt == null) {
            this.readAt = now;
        }
    }
}
