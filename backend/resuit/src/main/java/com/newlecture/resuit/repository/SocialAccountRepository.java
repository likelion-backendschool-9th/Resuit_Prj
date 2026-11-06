package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.SocialAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndSubject(String provider, String subject);

    List<SocialAccount> findByUser_Id(UUID userId);

    long countByUser_Id(UUID userId);

    Optional<SocialAccount> findByUser_IdAndProvider(UUID userId, String provider);
}
