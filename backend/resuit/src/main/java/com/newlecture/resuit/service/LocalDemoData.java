package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.Badge;
import com.newlecture.resuit.entity.BadgeStatus;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.PinnedRepo;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.entity.UserBadgeHistory;
import com.newlecture.resuit.repository.BadgeRepository;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.PinnedRepoRepository;
import com.newlecture.resuit.repository.UserBadgeHistoryRepository;
import com.newlecture.resuit.repository.UserBadgeRepository;
import com.newlecture.resuit.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * local 프로필용 데모 데이터.
 * 대표 배지 교체 확인을 위해 ACTIVE 배지를 여러 개 둔다.
 */
@Component
@Profile("local")
public class LocalDemoData implements CommandLineRunner {
    private final UserRepository users;
    private final CommonResumeRepository resumes;
    private final BadgeRepository badges;
    private final UserBadgeRepository userBadges;
    private final UserBadgeHistoryRepository badgeHistory;
    private final PinnedRepoRepository pinnedRepos;

    public LocalDemoData(UserRepository users, CommonResumeRepository resumes,
            BadgeRepository badges, UserBadgeRepository userBadges,
            UserBadgeHistoryRepository badgeHistory, PinnedRepoRepository pinnedRepos) {
        this.users = users;
        this.resumes = resumes;
        this.badges = badges;
        this.userBadges = userBadges;
        this.badgeHistory = badgeHistory;
        this.pinnedRepos = pinnedRepos;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime quarterStart = ReviewerBadgePolicy.currentQuarterStart(now.toLocalDate());
        LocalDateTime continuousSince = quarterStart.minusMonths(6);

        Badge reviewer = ensureBadge("REVIEWER", "REVIEWER", "리뷰어");
        Badge excellent = ensureBadge("EXCELLENT_REVIEWER", "REVIEWER", "우수 리뷰어");
        Badge techWriter = ensureBadge("TECH_WRITER", "ACHIEVEMENT", "기술 글 작성");
        Badge resumeHelper = ensureBadge("RESUME_HELPER", "ACHIEVEMENT", "이력서 도우미");

        User seoyeon = ensureUser("seoyeon_dev", "seoyeon@example.com", "이서연",
                "React와 TypeScript를 주로 사용하는 개발자입니다.");
        ensureResume(seoyeon, "프론트엔드 개발자 이력서", "FRONTEND");
        // 대표 배지 교체용: ACTIVE 3개
        ensureOwned(seoyeon, reviewer, BadgeStatus.ACTIVE, continuousSince, null);
        ensureOwned(seoyeon, excellent, BadgeStatus.ACTIVE, continuousSince.minusMonths(3), null);
        ensureOwned(seoyeon, techWriter, BadgeStatus.ACTIVE, continuousSince, null);
        ensureOwned(seoyeon, resumeHelper, BadgeStatus.GRACE, null,
                ReviewerBadgePolicy.nextQuarterStart(now.toLocalDate()));
        seoyeon.displayBadge(reviewer);
        ensureHistory(seoyeon, excellent, quarterStart.minusMonths(9), quarterStart.minusMonths(6));
        ensureHistory(seoyeon, reviewer, quarterStart.minusMonths(12), quarterStart.minusMonths(9));
        ensurePinnedRepos(seoyeon, List.of(
                "seoyeon-dev/sns-clone",
                "seoyeon-dev/portfolio",
                "seoyeon-dev/design-tokens"));

        User minseo = ensureUser("minseo_be", "minseo@example.com", "한민서",
                "Spring과 Docker를 다루는 백엔드 개발자입니다.");
        ensureResume(minseo, "백엔드 지원용 이력서", "BACKEND");
        ensureOwned(minseo, excellent, BadgeStatus.ACTIVE, continuousSince.minusMonths(9), null);
        ensureOwned(minseo, reviewer, BadgeStatus.ACTIVE, continuousSince.minusMonths(9), null);
        minseo.displayBadge(excellent);
        ensureHistory(minseo, excellent, quarterStart.minusMonths(12), quarterStart.minusMonths(6));
        ensurePinnedRepos(minseo, List.of("minseo-be/order-api", "minseo-be/deploy-pipeline"));

        User haeun = ensureUser("haeun_ux", "haeun@example.com", "최하은",
                "UX를 데이터로 검증하는 프론트엔드 개발자입니다.");
        ensureResume(haeun, "UX 중심 포트폴리오", "FRONTEND");
        ensureOwned(haeun, reviewer, BadgeStatus.GRACE, null,
                ReviewerBadgePolicy.nextQuarterStart(now.toLocalDate()));
        ensureOwned(haeun, techWriter, BadgeStatus.ACTIVE, continuousSince, null);
        haeun.displayBadge(techWriter);
        ensureHistory(haeun, reviewer, quarterStart.minusMonths(6), quarterStart);
        ensurePinnedRepos(haeun, List.of("haeun-ux/twitter-clone"));
    }

    private void ensurePinnedRepos(User user, List<String> names) {
        if (!pinnedRepos.findByUserIdOrderByOrderNoAsc(user.getId()).isEmpty()) {
            return;
        }
        for (int i = 0; i < names.size(); i++) {
            pinnedRepos.save(PinnedRepo.builder().user(user)
                    .repoFullName(names.get(i)).orderNo(i + 1).build());
        }
    }

    private User ensureUser(String username, String email, String nickname, String intro) {
        return users.findByUsername(username).orElseGet(() -> users.save(
                User.builder().username(username).email(email).role("USER")
                        .nickname(nickname).selfIntroduction(intro)
                        .widgets(Map.of("grass", true, "resumes", true)).build()));
    }

    private void ensureResume(User user, String title, String field) {
        boolean hasResume = resumes.findAll().stream()
                .anyMatch(item -> item.getUser().getId().equals(user.getId()));
        CommonResume resume = hasResume
                ? resumes.findAll().stream()
                    .filter(item -> item.getUser().getId().equals(user.getId()))
                    .findFirst().orElseThrow()
                : resumes.save(CommonResume.builder().user(user).title(title).field(field)
                        .content(Map.of("sections", Map.of())).isPublic(true).build());
        if (user.getPinnedCommonResume() == null) {
            user.pinResume(resume);
        }
    }

    private Badge ensureBadge(String code, String type, String name) {
        return badges.findByCode(code).orElseGet(() -> badges.save(
                Badge.builder().code(code).type(type).name(name).build()));
    }

    private void ensureOwned(User user, Badge badge, BadgeStatus status,
            LocalDateTime continuousSince, LocalDateTime graceUntil) {
        UserBadge owned = userBadges.findByIdUserIdAndIdBadgeId(user.getId(), badge.getId())
                .orElseGet(() -> userBadges.save(new UserBadge(user, badge)));
        if (status == BadgeStatus.ACTIVE) {
            owned.activate(continuousSince != null ? continuousSince : LocalDateTime.now());
        } else if (status == BadgeStatus.GRACE) {
            owned.requireRenewal(graceUntil != null ? graceUntil
                    : ReviewerBadgePolicy.nextQuarterStart(LocalDateTime.now().toLocalDate()));
        } else {
            owned.expire(LocalDateTime.now());
        }
    }

    private void ensureHistory(User user, Badge badge, LocalDateTime from, LocalDateTime until) {
        List<UserBadgeHistory> existing = badgeHistory.findByUserIdOrderByActiveUntilDesc(user.getId());
        boolean present = existing.stream().anyMatch(h ->
                h.getBadge().getId().equals(badge.getId())
                        && h.getActiveFrom().equals(from)
                        && h.getActiveUntil().equals(until));
        if (!present) {
            badgeHistory.save(new UserBadgeHistory(user, badge, from, until));
        }
    }
}
