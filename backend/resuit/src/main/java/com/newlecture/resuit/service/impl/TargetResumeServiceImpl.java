package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.TargetResumeCreateRequest;
import com.newlecture.resuit.dto.resume.TargetResumeDetailResponse;
import com.newlecture.resuit.dto.resume.TargetResumeSummary;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.TargetResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.TargetResumeService;

import java.util.LinkedHashMap;
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
public class TargetResumeServiceImpl implements TargetResumeService {
    private static final int MAX_TITLE = 100;

    private final TargetResumeRepository resumes;
    private final CommonResumeRepository commonResumes;
    private final UserRepository users;

    @Override
    @Transactional(readOnly = true)
    public List<TargetResumeSummary> getMyResumes(String username) {
        var user = currentUser(username);
        var mine = resumes.findMineWithJob(user.getId());
        if (mine.isEmpty()) return List.of();
        var jobIds = mine.stream().map(resume -> resume.getJob().getId()).collect(Collectors.toSet());
        Map<Long, Long> questionCounts = resumes.countQuestions(jobIds).stream().collect(Collectors.toMap(
            TargetResumeRepository.QuestionCountRow::getJobId, TargetResumeRepository.QuestionCountRow::getCount));
        Set<Long> submitted = Set.copyOf(resumes.findSubmittedIds(user.getId()));
        return mine.stream().map(resume -> TargetResumeSummary.of(resume,
            questionCounts.getOrDefault(resume.getJob().getId(), 0L), submitted.contains(resume.getId()))).toList();
    }

    // 특화 공고에만, 공고당 하나만 만든다. BASE는 고른 공통 이력서 내용을 복사해 두고
    // 이후 공통 이력서를 고쳐도 특화 이력서는 바뀌지 않는다.
    @Override
    @Transactional
    public TargetResumeDetailResponse create(String username, TargetResumeCreateRequest request) {
        var user = currentUser(username);
        if (request == null || request.jobId() == null) throw new InvalidResumeException("공고를 골라 주세요.");
        var job = resumes.findJob(request.jobId()).orElseThrow(JobNotFoundException::new);
        if (!"SPECIAL".equalsIgnoreCase(job.getType())) throw new NotSpecialJobException();
        if (resumes.existsByUserIdAndJobId(user.getId(), job.getId())) throw new TargetResumeExistsException();

        var mode = request.mode() == null ? (request.commonResumeId() == null ? "BLANK" : "BASE")
            : request.mode().trim().toUpperCase(Locale.ROOT);
        CommonResume base = null;
        Map<String, Object> content = new LinkedHashMap<>();
        switch (mode) {
            case "BASE" -> {
                if (request.commonResumeId() == null) throw new InvalidResumeException("불러올 공통 이력서를 골라 주세요.");
                base = commonResumes.findById(request.commonResumeId()).orElseThrow(ResumeNotFoundException::new);
                if (!base.getUser().getId().equals(user.getId())) throw new ResumeForbiddenException();
                content = copyOf(base.getContent());
            }
            case "BLANK" -> { }
            default -> throw new InvalidResumeException("시작 방식은 BASE 또는 BLANK여야 해요.");
        }

        var title = request.title() == null ? job.getTitle() : validTitle(request.title());
        var resume = TargetResume.builder()
            .job(job)
            .user(user)
            .commonResume(base)
            .title(title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title)
            .content(content)
            .build();
        return TargetResumeDetailResponse.of(resumes.save(resume), resumes.findQuestions(job.getId()), false);
    }

    // JSON 내용을 깊게 복사한다. 공통 이력서와 같은 목록 객체를 나눠 쓰지 않게 하려고.
    @SuppressWarnings("unchecked")
    private static <T> T copyOf(T value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, item) -> copy.put(String.valueOf(key), copyOf(item)));
            return (T) copy;
        }
        if (value instanceof List<?> list) return (T) list.stream().map(TargetResumeServiceImpl::copyOf).collect(Collectors.toList());
        return value;
    }

    private User currentUser(String username) {
        if (username == null || username.isBlank()) throw new UnauthenticatedUserException();
        return users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
    }

    private TargetResume ownedResume(Long resumeId, User user) {
        if (resumeId == null || resumeId <= 0) throw new ResumeNotFoundException();
        var resume = resumes.findById(resumeId).orElseThrow(ResumeNotFoundException::new);
        if (!resume.getUser().getId().equals(user.getId())) throw new ResumeForbiddenException();
        return resume;
    }

    private boolean isSubmitted(TargetResume resume, User user) {
        return resumes.findSubmittedIds(user.getId()).contains(resume.getId());
    }

    private static String validTitle(String title) {
        if (title == null || title.isBlank()) throw new InvalidResumeException("이력서 이름을 입력해 주세요.");
        var trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) throw new InvalidResumeException("이력서 이름은 " + MAX_TITLE + "자까지 쓸 수 있어요.");
        return trimmed;
    }
}
