package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanyOffer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyOfferRepository extends JpaRepository<CompanyOffer, Long> {

    // 내가 받은 제안을 최신순으로 읽는다. 보낸 기업·받은 회원·공고를 함께 읽어 N+1 문제를 막는다.
    @EntityGraph(attributePaths = {"company", "user", "job"})
    List<CompanyOffer> findByUser_IdOrderByIdDesc(UUID userId);

    // 우리 회사가 보낸 제안을 최신순으로 읽는다. 받은 회원·공고를 함께 읽어 N+1 문제를 막는다.
    @EntityGraph(attributePaths = {"company", "user", "job"})
    List<CompanyOffer> findByCompany_IdOrderByIdDesc(UUID companyId);
}
