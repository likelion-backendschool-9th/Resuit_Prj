package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanyOffer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyOfferRepository extends JpaRepository<CompanyOffer, Long> {

    @EntityGraph(attributePaths = {"company", "user", "job"})
    List<CompanyOffer> findByUser_IdOrderByIdDesc(UUID userId);
}
