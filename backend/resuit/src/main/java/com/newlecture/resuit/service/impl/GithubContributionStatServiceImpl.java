package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.entity.GithubContributionStat;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.GithubContributionStatRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.GithubContributionService;
import com.newlecture.resuit.service.GithubContributionStatService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GithubContributionStatServiceImpl implements GithubContributionStatService {

    private final GithubContributionStatRepository stats;
    private final UserRepository users;
    private final GithubContributionService githubContributions;

    public GithubContributionStatServiceImpl(GithubContributionStatRepository stats, UserRepository users,
            GithubContributionService githubContributions) {
        this.stats = stats;
        this.users = users;
        this.githubContributions = githubContributions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findSyncTargets() {
        return stats.findGithubConnectedUsernames();
    }

    @Override
    @Transactional
    public void sync(String username) {
        int total = githubContributions.getContributions(username, null, null).totalContributions();
        User user = users.findByUsername(username).orElse(null);
        if (user == null) return;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        stats.findById(user.getId()).ifPresentOrElse(
                stat -> stat.update(total, now),
                () -> stats.save(new GithubContributionStat(user, total, now)));
    }
}
