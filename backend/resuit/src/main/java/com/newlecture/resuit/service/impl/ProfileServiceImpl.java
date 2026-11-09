package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.CareerRequest;
import com.newlecture.resuit.dto.CareerResponse;
import com.newlecture.resuit.dto.DisplayedBadgeRequest;
import com.newlecture.resuit.dto.FollowStatusResponse;
import com.newlecture.resuit.dto.MyProfileResponse;
import com.newlecture.resuit.dto.PageResponse;
import com.newlecture.resuit.dto.PinnedResumeRequest;
import com.newlecture.resuit.dto.ProfileFeedbackResponse;
import com.newlecture.resuit.dto.ProfilePostResponse;
import com.newlecture.resuit.dto.ProfileResponse;
import com.newlecture.resuit.dto.ResumeVisibilityRequest;
import com.newlecture.resuit.dto.UpdateProfileRequest;
import com.newlecture.resuit.entity.BadgeStatus;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.Follow;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.entity.UserCareer;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.FollowRepository;
import com.newlecture.resuit.repository.ProfileFeedbackRepository;
import com.newlecture.resuit.repository.ProfilePostRepository;
import com.newlecture.resuit.repository.UserBadgeHistoryRepository;
import com.newlecture.resuit.repository.UserBadgeRepository;
import com.newlecture.resuit.repository.UserCareerRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.VerificationRepository;
import com.newlecture.resuit.service.MediaService;
import com.newlecture.resuit.service.ProfileService;
import com.newlecture.resuit.service.ReviewerBadgePolicy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository users;
    private final CommonResumeRepository resumes;
    private final FollowRepository follows;
    private final ProfilePostRepository posts;
    private final ProfileFeedbackRepository feedbacks;
    private final UserBadgeRepository userBadges;
    private final UserBadgeHistoryRepository badgeHistory;
    private final UserCareerRepository careers;
    private final VerificationRepository verifications;
    private final MediaService mediaService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(String username, UUID viewerId) {
        return toResponse(findByUsername(username), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(UUID userId) {
        return toMyResponse(findById(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfilePostResponse> getPosts(String username, String type,
            int page, int size) {
        User user = findByUsername(username);
        PageRequest paging = paging(page, size, "createdAt");
        Page<ProfilePostResponse> result = ("tech".equalsIgnoreCase(type)
                ? posts.findByUserIdAndTypeAndDeletedAtIsNull(user.getId(), "TECH", paging)
                : posts.findByUserIdAndDeletedAtIsNull(user.getId(), paging))
                .map(post -> new ProfilePostResponse(post.getId(), post.getType(),
                        post.getContent(), post.getImageUrl(), post.getVideoUrl(),
                        post.getCreatedAt()));
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfileFeedbackResponse> getFeedbacks(String username,
            int page, int size) {
        User user = findByUsername(username);
        Page<ProfileFeedbackResponse> result = feedbacks.findByUserId(user.getId(),
                paging(page, size, "createdAt"))
                .map(comment -> new ProfileFeedbackResponse(comment.getCommentId(),
                        comment.getFeedbackRequest().getId(), comment.getSection(),
                        comment.getContent(), comment.isAdopted(), comment.getCreatedAt()));
        return PageResponse.from(result);
    }

    @Override
    @Transactional
    public MyProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);
        if (request.nickname() != null && request.nickname().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임은 50자 이하여야 합니다.");
        }
        if (request.selfIntroduction() != null && request.selfIntroduction().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기소개는 500자 이하여야 합니다.");
        }
        String profileImageUrl = resolveProfileImageUrl(userId, request.profileImageUrl());
        if (profileImageUrl != null && profileImageUrl.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 사진 주소가 너무 깁니다.");
        }
        user.updateProfile(request.nickname(), request.selfIntroduction(),
                profileImageUrl, normalizeWidgets(request.widgets()));
        // 지역은 고른 경우에만 바꾼다. 비어 있으면 기존 값을 유지한다.
        if (request.region() != null) {
            user.changeRegion(request.region().name());
        }
        replaceCareers(user, request.careers());
        return toMyResponse(user);
    }

    /** null이면 경력을 유지하고, 목록이면 기존 줄을 그 순서로 바꾼다. */
    private void replaceCareers(User user, List<CareerRequest> requested) {
        if (requested == null) {
            return;
        }
        if (requested.size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경력은 20개까지 저장할 수 있습니다.");
        }
        careers.deleteAll(careers.findByUser_IdOrderByDisplayOrderAsc(user.getId()));
        careers.flush();
        int order = 1;
        for (CareerRequest item : requested) {
            careers.save(UserCareer.builder()
                    .user(user)
                    .companyName(requiredText(item.companyName(), 100,
                            "회사명을 입력해 주세요.", "회사명은 100자 이하여야 합니다."))
                    .jobTitle(requiredText(item.jobTitle(), 100,
                            "직무를 입력해 주세요.", "직무는 100자 이하여야 합니다."))
                    .startDate(requiredDate(item.startDate()))
                    .endDate(validEndDate(item.startDate(), item.endDate()))
                    .description(optionalText(item.description(), 1000))
                    .displayOrder(order++)
                    .build());
        }
    }

    private static String requiredText(String raw, int max, String missing, String tooLong) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, missing);
        }
        String value = raw.trim();
        if (value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, tooLong);
        }
        return value;
    }

    private static String optionalText(String raw, int max) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        if (value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경력 설명은 " + max + "자 이하여야 합니다.");
        }
        return value;
    }

    private static LocalDate requiredDate(LocalDate startDate) {
        if (startDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경력 시작일이 필요합니다.");
        }
        return startDate;
    }

    private static LocalDate validEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경력 종료일은 시작일보다 빠를 수 없습니다.");
        }
        return endDate;
    }

    /** null은 미변경, 빈 문자열은 삭제, 임시 업로드는 게시 폴더로 옮긴다. */
    private String resolveProfileImageUrl(UUID userId, String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("/media/image/")) {
            return value;
        }
        String relative = value.startsWith("/media/") ? value.substring("/media/".length()) : value;
        if (relative.startsWith("tmpImage/") || relative.startsWith("image/")) {
            try {
                if (relative.startsWith("image/")) {
                    return "/media/" + relative.replace('\\', '/');
                }
                String published = mediaService.finalizeForPost(relative, userId, "IMAGE");
                return published == null ? "" : "/media/" + published;
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "프로필 사진은 파일 업로드 또는 https:// 주소만 사용할 수 있습니다.");
    }

    @Override
    @Transactional
    public MyProfileResponse setResumeVisibility(UUID userId, Long resumeId,
            ResumeVisibilityRequest request) {
        User user = findById(userId);
        CommonResume resume = resumes.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "공통 이력서를 찾을 수 없습니다."));
        resume.setPublic(request.isPublic());
        if (!request.isPublic() && user.getPinnedCommonResume() != null
                && user.getPinnedCommonResume().getId().equals(resumeId)) {
            user.pinResume(null);
        }
        return toMyResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse pinResume(UUID userId, PinnedResumeRequest request) {
        User user = findById(userId);
        CommonResume resume = null;
        if (request.cResumeId() != null) {
            resume = resumes.findByIdAndUserIdAndIsPublicTrue(request.cResumeId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "본인이 공개한 공통 이력서만 고정할 수 있습니다."));
        }
        user.pinResume(resume);
        return toResponse(user, userId);
    }

    @Override
    @Transactional
    public ProfileResponse displayBadge(UUID userId, DisplayedBadgeRequest request) {
        User user = findById(userId);
        if (request.badgeId() == null) {
            user.displayBadge(null);
        } else {
            UserBadge owned = userBadges.findByIdUserIdAndIdBadgeId(userId, request.badgeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "보유하지 않은 배지입니다."));
            if (owned.getStatus() != BadgeStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "활성 배지만 대표 배지로 선택할 수 있습니다.");
            }
            user.displayBadge(owned.getBadge());
        }
        return toResponse(user, userId);
    }

    @Override
    @Transactional
    public FollowStatusResponse follow(UUID followerId, String targetUsername) {
        User follower = findById(followerId);
        User target = findByUsername(targetUsername);
        if (follower.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우할 수 없습니다.");
        }
        if (!follows.existsByFollowerIdAndFollowingId(follower.getId(), target.getId())) {
            follows.save(new Follow(follower, target));
        }
        return followStatus(follower.getId(), target);
    }

    @Override
    @Transactional
    public FollowStatusResponse unfollow(UUID followerId, String targetUsername) {
        User target = findByUsername(targetUsername);
        follows.deleteByFollowerIdAndFollowingId(followerId, target.getId());
        return followStatus(followerId, target);
    }

    private FollowStatusResponse followStatus(UUID viewerId, User target) {
        return new FollowStatusResponse(
                follows.existsByFollowerIdAndFollowingId(viewerId, target.getId()),
                follows.countByFollowingId(target.getId()),
                follows.countByFollowerId(target.getId()));
    }

    private Map<String, Object> normalizeWidgets(Map<String, Object> widgets) {
        if (widgets == null) return null;
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("grass", truthy(widgets.get("grass")) || truthy(widgets.get("GRASS")));
        normalized.put("resumes", truthy(widgets.get("resumes")) || truthy(widgets.get("PINNED_RESUME")));
        return normalized;
    }

    private static boolean truthy(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private PageRequest paging(int page, int size, String sortProperty) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 페이지 조건입니다.");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortProperty));
    }

    private MyProfileResponse toMyResponse(User user) {
        List<MyProfileResponse.OwnedResumeSummary> owned = resumes
                .findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(r -> new MyProfileResponse.OwnedResumeSummary(r.getId(), r.getTitle(),
                        r.getField(), r.isPublic()))
                .toList();
        return new MyProfileResponse(toResponse(user, user.getId()), user.getEmail(), owned);
    }

    private ProfileResponse toResponse(User user, UUID viewerId) {
        UUID id = user.getId();
        List<ProfileResponse.BadgeSummary> badges = userBadges.findByIdUserId(id).stream()
                .map(this::toBadgeSummary).toList();
        ProfileResponse.BadgeSummary displayed = badges.stream()
                .filter(b -> user.getDisplayedBadge() != null
                        && b.id().equals(user.getDisplayedBadge().getId())
                        && b.status() == BadgeStatus.ACTIVE)
                .findFirst().orElse(null);
        CommonResume pinned = user.getPinnedCommonResume();
        ProfileResponse.ResumeSummary resume = pinned != null && pinned.isPublic()
                ? new ProfileResponse.ResumeSummary(pinned.getId(), pinned.getTitle(), pinned.getField())
                : null;
        List<ProfileResponse.BadgeHistorySummary> history = badgeHistory
                .findByUserIdOrderByActiveUntilDesc(id).stream()
                .map(h -> new ProfileResponse.BadgeHistorySummary(h.getBadge().getId(),
                        h.getBadge().getName(), h.getActiveFrom(), h.getActiveUntil()))
                .toList();
        boolean isFollowing = viewerId != null && !viewerId.equals(id)
                && follows.existsByFollowerIdAndFollowingId(viewerId, id);
        List<CareerResponse> careerItems = careers.findByUser_IdOrderByDisplayOrderAsc(id).stream()
                .map(item -> new CareerResponse(item.getId(), item.getCompanyName(),
                        item.getJobTitle(), item.getStartDate(), item.getEndDate(),
                        item.getDescription(), item.getDisplayOrder()))
                .toList();
        // 공개 프로필에는 기업 인증 여부만 담는다. 이메일은 /verifications/me 전용이다.
        var companyVerification = verifications
                .findFirstByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNullOrderByVerifiedAtDesc(id, "company_email");
        boolean companyVerified = companyVerification.isPresent();
        LocalDateTime companyVerifiedAt = companyVerification.map(v -> v.getVerifiedAt()).orElse(null);
        return new ProfileResponse(id, user.getUsername(), user.getNickname(),
                user.getProfileImageUrl(), user.getSelfIntroduction(), user.getWidgets(),
                follows.countByFollowingId(id), follows.countByFollowerId(id),
                posts.countByUserIdAndDeletedAtIsNull(id), feedbacks.countByUserId(id),
                isFollowing, resume, displayed, badges, history, careerItems,
                companyVerified, companyVerifiedAt, user.getRegion());
    }

    private ProfileResponse.BadgeSummary toBadgeSummary(UserBadge owned) {
        LocalDateTime continuousSince = owned.getContinuousSince();
        int streak = owned.getStatus() == BadgeStatus.ACTIVE
                ? ReviewerBadgePolicy.streakQuarters(continuousSince, LocalDateTime.now())
                : 0;
        return new ProfileResponse.BadgeSummary(owned.getBadge().getId(),
                owned.getBadge().getCode(), owned.getBadge().getName(),
                owned.getBadge().getIconUrl(), owned.getStatus(), owned.getAcquiredAt(),
                owned.getGraceUntil(), owned.getExpiresAt(), continuousSince, streak);
    }

    private User findByUsername(String username) {
        return users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private User findById(UUID id) {
        return users.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
