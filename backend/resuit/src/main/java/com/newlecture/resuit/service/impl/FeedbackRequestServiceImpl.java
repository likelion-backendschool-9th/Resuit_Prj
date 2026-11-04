package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.feedback.FeedbackRequestCreateRequest;
import com.newlecture.resuit.dto.feedback.FeedbackRequestResponse;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.FeedbackRequest;
import com.newlecture.resuit.entity.FeedbackRequestSection;
import com.newlecture.resuit.entity.TargetResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.FeedbackRequestRepository;
import com.newlecture.resuit.repository.FeedbackRequestSectionRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.FeedbackRequestService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackRequestServiceImpl implements FeedbackRequestService {
    private static final int MAX_CONTENT = 1000;
    private static final Set<String> CATEGORIES = Set.of("FRONTEND", "BACKEND");
    // docs/database-design.md 이력서 항목. ANSWERS는 특화 이력서의 요구 문항 답변
    private static final Set<String> SECTIONS = Set.of("BASIC", "SKILLS", "PROJECTS", "AWARDS", "INTRODUCTION", "ANSWERS");

    private final FeedbackRequestRepository requests;
    private final FeedbackRequestSectionRepository sections;
    private final CommonResumeRepository commonResumes;
    private final TargetResumeRepository targetResumes;
    private final UserRepository users;

    // 본인 이력서 하나에 대해, 진행 중인 요청이 없을 때만 공개 피드백 게시판에 올린다.
    @Override
    @Transactional
    public FeedbackRequestResponse create(String username, FeedbackRequestCreateRequest request) {
        var user = currentUser(username);
        if (request == null) throw new InvalidResumeException("피드백 요청 정보가 없어요.");
        if ((request.commonResumeId() == null) == (request.targetResumeId() == null)) {
            throw new InvalidResumeException("공통 이력서와 특화 이력서 중 하나만 골라 주세요.");
        }

        CommonResume common = null;
        TargetResume target = null;
        String resumeField;
        if (request.commonResumeId() != null) {
            common = commonResumes.findById(request.commonResumeId()).orElseThrow(ResumeNotFoundException::new);
            checkOwner(common.getUser(), user);
            if (commonResumes.hasActiveFeedbackRequest(common.getId())) throw new FeedbackRequestExistsException();
            resumeField = common.getField();
        } else {
            target = targetResumes.findById(request.targetResumeId()).orElseThrow(ResumeNotFoundException::new);
            checkOwner(target.getUser(), user);
            if (targetResumes.hasActiveFeedbackRequest(target.getId())) throw new FeedbackRequestExistsException();
            resumeField = target.getCommonResume() == null ? null : target.getCommonResume().getField();
        }

        var saved = requests.save(FeedbackRequest.builder()
            .user(user)
            .commonResume(common)
            .targetResume(target)
            .content(validContent(request.content()))
            .category(validCategory(request.category() == null ? resumeField : request.category()))
            .build());
        var sectionTypes = validSections(request.sections());
        sections.saveAll(sectionTypes.stream()
            .map(type -> FeedbackRequestSection.builder().request(saved).sectionType(type).build()).toList());
        return FeedbackRequestResponse.of(saved, sectionTypes);
    }

    private User currentUser(String username) {
        if (username == null || username.isBlank()) throw new UnauthenticatedUserException();
        return users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
    }

    private static void checkOwner(User owner, User user) {
        if (!owner.getId().equals(user.getId())) throw new ResumeForbiddenException();
    }

    private static String validContent(String content) {
        if (content == null || content.isBlank()) return null;
        var trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT) throw new InvalidResumeException("요청 메시지는 " + MAX_CONTENT + "자까지 쓸 수 있어요.");
        return trimmed;
    }

    private static String validCategory(String category) {
        if (category == null || category.isBlank()) return null;
        var normalized = category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(normalized)) throw new InvalidResumeException("분야는 FRONTEND 또는 BACKEND여야 해요.");
        return normalized;
    }

    // 같은 항목을 두 번 보내도 한 번만 저장한다.
    private static List<String> validSections(List<String> values) {
        if (values == null) return List.of();
        var result = new LinkedHashSet<String>();
        for (var value : values) {
            var normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
            if (!SECTIONS.contains(normalized)) throw new InvalidResumeException("봐 줄 항목 값이 올바르지 않아요: " + value);
            result.add(normalized);
        }
        return List.copyOf(result);
    }
}
