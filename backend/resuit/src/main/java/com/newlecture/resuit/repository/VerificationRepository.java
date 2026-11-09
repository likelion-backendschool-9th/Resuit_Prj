package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Verification;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findFirstByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNullOrderByVerifiedAtDesc(
            UUID userId, String type);

    List<Verification> findByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNull(
            UUID userId, String type);

    // 여러 회원의 유효한 인증 기록을 한 번에 읽는다. 채용 제안 목록에 기업 인증 이메일을 붙일 때 쓴다.
    List<Verification> findByUserIdInAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNull(
            Collection<UUID> userIds, String type);
}
