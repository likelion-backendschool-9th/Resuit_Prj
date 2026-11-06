package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanyEmailDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyEmailDomainRepository extends JpaRepository<CompanyEmailDomain, Long> {

    boolean existsByDomain(String domain);
}
