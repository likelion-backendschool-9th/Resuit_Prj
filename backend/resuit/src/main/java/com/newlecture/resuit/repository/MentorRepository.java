package com.newlecture.resuit.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Mentor;

public interface MentorRepository extends JpaRepository<Mentor, UUID> {

	// 현직자 인증(회사 이메일 인증)이 지금 유효한 현직자만 고르는 조건. 인증을 취소하면 목록·상세·요청에서 빠진다.
	String ACTIVE = "exists (select v from Verification v where v.user.id = m.userId and v.type = 'company_email' and v.verifiedAt is not null and v.revokedAt is null)";

	// 유효한 현직자를 인증일 최신순으로 페이지 조회한다.
	@EntityGraph(attributePaths = "user")
	@Query(value = "select m from Mentor m where " + ACTIVE + " order by m.verifiedAt desc, m.userId desc",
			countQuery = "select count(m) from Mentor m where " + ACTIVE)
	Page<Mentor> findActive(Pageable pageable);

	// 유효한 현직자 한 명
	@EntityGraph(attributePaths = "user")
	@Query("select m from Mentor m where m.userId = :id and " + ACTIVE)
	Optional<Mentor> findActiveById(@Param("id") UUID id);

	@EntityGraph(attributePaths = "user")
	Page<Mentor> findAllByOrderByVerifiedAtDescUserIdDesc(Pageable pageable);
}
