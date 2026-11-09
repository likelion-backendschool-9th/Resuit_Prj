package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanyEmailDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyEmailDomainRepository extends JpaRepository<CompanyEmailDomain, Long> {

    boolean existsByDomain(String domain);

    // 도메인 행. 현직자의 회사명을 정할 때 쓴다.
    java.util.Optional<CompanyEmailDomain> findByDomain(String domain);
}
