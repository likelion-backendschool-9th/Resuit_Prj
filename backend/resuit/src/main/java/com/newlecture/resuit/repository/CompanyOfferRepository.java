package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanyOffer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyOfferRepository extends JpaRepository<CompanyOffer, Long> {

    // 내가 받은 제안을 최신순으로 읽는다. 보낸 기업·받은 회원·공고를 함께 읽어 N+1 문제를 막는다.
    @EntityGraph(attributePaths = {"company", "user", "job"})
    List<CompanyOffer> findByUser_IdOrderByIdDesc(UUID userId);

    // 우리 회사가 보낸 제안을 최신순으로 읽는다. 받은 회원·공고를 함께 읽어 N+1 문제를 막는다.
    @EntityGraph(attributePaths = {"company", "user", "job"})
    List<CompanyOffer> findByCompany_IdOrderByIdDesc(UUID companyId);

    @EntityGraph(attributePaths = {"company", "user", "job"})
    Optional<CompanyOffer> findByIdAndUser_Id(Long id, UUID userId);

    // 같은 기업이 같은 회원에게 이미 제안을 보냈는지. 제안은 한 번만 보낼 수 있다.
    boolean existsByCompany_IdAndUser_Id(UUID companyId, UUID userId);

    // 이 기업이 이미 제안을 보낸 회원 ID. 인재 목록 카드에 "제안 보냄"을 표시할 때 쓴다.
    @Query("SELECT o.user.id FROM CompanyOffer o WHERE o.company.id = :companyId AND o.user.id IN :userIds")
    List<UUID> findOfferedUserIds(@Param("companyId") UUID companyId, @Param("userIds") Collection<UUID> userIds);
}
