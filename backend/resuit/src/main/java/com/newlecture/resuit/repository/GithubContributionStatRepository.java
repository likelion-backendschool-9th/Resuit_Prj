package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.GithubContributionStat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GithubContributionStatRepository extends JpaRepository<GithubContributionStat, UUID> {

    // integrations 테이블을 읽지만, 갱신 대상을 고르는 용도라 이 리포지토리에 둔다.
    @Query("""
        SELECT i.user.username FROM Integration i
        WHERE i.provider = 'GITHUB' AND i.connected = true
        """)
    List<String> findGithubConnectedUsernames();
}
