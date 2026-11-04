package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.TargetResumeSummary;
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

import java.util.List;
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
