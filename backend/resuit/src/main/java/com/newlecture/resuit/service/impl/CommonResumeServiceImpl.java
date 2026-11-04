package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonResumeServiceImpl implements CommonResumeService {
    private static final int MAX_TITLE = 100;
    private static final Set<String> FIELDS = Set.of("FRONTEND", "BACKEND");

    private final CommonResumeRepository resumes;
    private final UserRepository users;

    @Override
    @Transactional(readOnly = true)
    public List<CommonResumeSummary> getMyResumes(String username) {
        var user = currentUser(username);
        Map<Long, Long> feedbackCounts = toMap(resumes.countFeedbackByUser(user.getId()));
        Map<Long, Long> targetCounts = toMap(resumes.countTargetResumesByUser(user.getId()));
        return resumes.findByUserIdOrderByIdAsc(user.getId()).stream()
            .map(resume -> CommonResumeSummary.of(resume,
                feedbackCounts.getOrDefault(resume.getId(), 0L), targetCounts.getOrDefault(resume.getId(), 0L)))
            .toList();
    }

    private static Map<Long, Long> toMap(List<CommonResumeRepository.CountRow> rows) {
        return rows.stream().collect(Collectors.toMap(CommonResumeRepository.CountRow::getResumeId,
            CommonResumeRepository.CountRow::getCount));
    }

    private User currentUser(String username) {
        if (username == null || username.isBlank()) throw new UnauthenticatedUserException();
        return users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
    }

    private CommonResume ownedResume(Long resumeId, User user) {
        if (resumeId == null || resumeId <= 0) throw new ResumeNotFoundException();
        var resume = resumes.findById(resumeId).orElseThrow(ResumeNotFoundException::new);
        if (!resume.getUser().getId().equals(user.getId())) throw new ResumeForbiddenException();
        return resume;
    }

    private static String validTitle(String title) {
        if (title == null || title.isBlank()) throw new InvalidResumeException("이력서 이름을 입력해 주세요.");
        var trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) throw new InvalidResumeException("이력서 이름은 " + MAX_TITLE + "자까지 쓸 수 있어요.");
        return trimmed;
    }

    // 직무 분야는 FRONTEND/BACKEND만 받는다. 소문자로 와도 대문자로 저장한다.
    private static String validField(String field) {
        if (field == null || field.isBlank()) return null;
        var normalized = field.trim().toUpperCase(Locale.ROOT);
        if (!FIELDS.contains(normalized)) throw new InvalidResumeException("직무 분야는 FRONTEND 또는 BACKEND여야 해요.");
        return normalized;
    }
}
