package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.RestoredResumeResponse;
import com.newlecture.resuit.dto.resume.SubmittedResumeSummary;
import com.newlecture.resuit.dto.resume.TargetResumeCreateRequest;
import com.newlecture.resuit.dto.resume.TargetResumeDetailResponse;
import com.newlecture.resuit.dto.resume.TargetResumeSummary;
import com.newlecture.resuit.dto.resume.TargetResumeUpdateCommand;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.entity.JobQuestion;
import com.newlecture.resuit.entity.SubmittedResume;
import com.newlecture.resuit.entity.TargetResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeInUseException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.MediaService;
import com.newlecture.resuit.service.TargetResumeService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TargetResumeServiceImpl implements TargetResumeService {
    private static final int MAX_TITLE = 100;

    private final TargetResumeRepository resumes;
    private final CommonResumeRepository commonResumes;
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final MediaService media;

    @Override
    @Transactional
    public RestoredResumeResponse restore(JobApplication application) {
        var snapshot = application.getSubmittedResumeSnapshot();
        var title = SubmittedResumeSummary.restoreTitle(application);
        var job = application.getJob();
        if (job.getDeletedAt() != null || !"SPECIAL".equalsIgnoreCase(job.getType()))
            throw new InvalidResumeException("삭제되었거나 유형이 변경된 공고의 특화 이력서는 복원할 수 없습니다.");
        if (resumes.existsByUserIdAndJobId(application.getUser().getId(), job.getId()))
            throw new InvalidResumeException("이 공고의 특화 이력서가 이미 있습니다. 기존 이력서를 삭제한 뒤 불러와 주세요.");
        Map<String, Object> content = new LinkedHashMap<>(SubmittedResume.copyContent(snapshot.content()));
        content.put("submittedAnswers", snapshot.content().getOrDefault("answers", List.of()));
        // Preserve the historic question text alongside answers; submission validates current questions again.
        content.put("submittedQuestions", snapshot.questions().stream().map(q -> {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("id", q.id()); question.put("content", q.content());
            question.put("charLimit", q.charLimit()); question.put("orderNo", q.orderNo());
            return question;
        }).toList());
        var resume = TargetResume.builder().user(application.getUser()).job(job).title(title).content(content)
            .photoUrl(media.restoreResumePhoto(application.getSnapshotPhotoKey(), application.getUser().getId())).build();
        resumes.saveAndFlush(resume);
        return new RestoredResumeResponse(resume.getId(), "TARGET", title);
    }

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
        var user = users.findWithLockByUsername(username).orElseThrow(UnauthenticatedUserException::new);
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
            .photoUrl(base == null ? null : media.copyResumePhoto(base.getPhotoUrl(), user.getId()))
            .title(title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title)
            .content(content)
            .build();
        return TargetResumeDetailResponse.of(resumes.save(resume), resumes.findQuestions(job.getId()), false);
    }

    // PATCH라서 보내지 않은(null) 값은 그대로 둔다. 답변은 이 공고의 문항에만, 글자 수 제한 안에서 받는다.
    @Override
    @Transactional
    public TargetResumeDetailResponse update(TargetResumeUpdateCommand command) {
        var user = currentUser(command.username());
        var resume = ownedResume(command.resumeId(), user);
        var request = command.request();
        if (request == null) throw new InvalidResumeException("이력서 정보가 없어요.");
        var questions = resumes.findQuestions(resume.getJob().getId());
        if (request.content() != null) validateAnswers(request.content().get("answers"), questions);
        resume.update(
            request.title() == null ? resume.getTitle() : validTitle(request.title()),
            request.content() == null ? resume.getContent() : request.content());
        if (request.photoUrl() != null) resume.updatePhoto(media.prepareResumePhoto(request.photoUrl(), user.getId()));
        resumes.flush();
        return TargetResumeDetailResponse.of(resume, questions, isSubmitted(resume, user));
    }

    // 지원·피드백 요청·유료 피드백이 이 이력서를 가리키면 DB의 FK가 삭제를 막는다.
    @Override
    @Transactional
    public void delete(Long resumeId, String username) {
        var user = users.findWithLockByUsername(username).orElseThrow(UnauthenticatedUserException::new);
        var resume = ownedResume(resumeId, user);
        try {
            applications.detachTarget(resumeId);
            resumes.delete(resume);
            resumes.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResumeInUseException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TargetResumeDetailResponse getMyResume(Long resumeId, String username) {
        var user = currentUser(username);
        var resume = ownedResume(resumeId, user);
        return TargetResumeDetailResponse.of(resume, resumes.findQuestions(resume.getJob().getId()), isSubmitted(resume, user));
    }

    public static void validateAnswers(Object answers, List<JobQuestion> questions) {
        if (answers == null) return;
        if (!(answers instanceof List<?> list)) throw new InvalidResumeException("답변 목록 형식이 올바르지 않아요.");
        Map<Long, JobQuestion> byId = questions.stream().collect(Collectors.toMap(JobQuestion::getId, q -> q));
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> answer) || !(answer.get("questionId") instanceof Number id)) {
                throw new InvalidResumeException("답변마다 questionId가 있어야 해요.");
            }
            var question = byId.get(id.longValue());
            if (question == null) throw new InvalidResumeException("이 공고에 없는 문항의 답변이 있어요.");
            var text = answer.get("answer");
            if (text != null && !(text instanceof String)) throw new InvalidResumeException("답변은 글자여야 해요.");
            if (text instanceof String s && question.getCharLimit() != null && s.length() > question.getCharLimit()) {
                throw new InvalidResumeException("문항 " + question.getOrderNo() + "의 답변은 " + question.getCharLimit() + "자까지 쓸 수 있어요.");
            }
        }
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
