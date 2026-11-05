package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Integration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {
    Optional<Integration> findByUserIdAndProvider(UUID userId, String provider);
    List<Integration> findByUserId(UUID userId);
    List<Integration> findByProviderAndConnectedTrue(String provider);
}
