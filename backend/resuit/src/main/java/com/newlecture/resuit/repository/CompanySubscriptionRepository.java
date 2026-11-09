package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CompanySubscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, UUID> {
}
