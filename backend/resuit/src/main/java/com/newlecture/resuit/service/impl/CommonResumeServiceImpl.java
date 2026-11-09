package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.dto.resume.CommonResumeRequest;
import com.newlecture.resuit.dto.resume.CommonResumeSummary;
import com.newlecture.resuit.dto.resume.CommonResumeUpdateCommand;
import com.newlecture.resuit.dto.resume.RestoredResumeResponse;
import com.newlecture.resuit.dto.resume.SubmittedResumeSummary;
import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.entity.SubmittedResume;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository;
import com.newlecture.resuit.repository.FeedbackCommentRepository;
import com.newlecture.resuit.repository.FeedbackRequestRepository;
import com.newlecture.resuit.repository.FeedbackRequestSectionRepository;
import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeInUseException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.MediaService;
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
public class CommonResumeServiceImpl implements CommonResumeService {
    private static final int MAX_TITLE = 100;
    private static final Set<String> FIELDS = Set.of("FRONTEND", "BACKEND");

    private final CommonResumeRepository resumes;
    private final FeedbackBoardRepository feedbackBoard;
    private final FeedbackRequestRepository feedbackRequests;
    private final FeedbackRequestSectionRepository feedbackSections;
    private final FeedbackCommentRepository feedbackComments;
    private final TargetResumeRepository targetResumes;
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final MediaService media;

    @Override
    @Transactional
    public RestoredResumeResponse restore(JobApplication application) {
        var snapshot = application.getSubmittedResumeSnapshot();
        var title = SubmittedResumeSummary.restoreTitle(application);
        var resume = CommonResume.builder().user(application.getUser()).title(title).field(snapshot.field())
            .content(SubmittedResume.copyContent(snapshot.content()))
            .photoUrl(media.restoreResumePhoto(application.getSnapshotPhotoKey(), application.getUser().getId())).build();
        resumes.saveAndFlush(resume);
        return new RestoredResumeResponse(resume.getId(), "COMMON", title);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommonResumeSummary> getMyResumes(String username) {
        var user = currentUser(username);
        Map<Long, Long> feedbackCounts = toMap(resumes.countFeedbackByUser(user.getId()));
        Map<Long, Long> targetCounts = toMap(resumes.countTargetResumesByUser(user.getId()));
        Map<Long, Long> activeRequests = resumes.findActiveFeedbackRequests(user.getId()).stream().collect(
            Collectors.toMap(CommonResumeRepository.ActiveRequestRow::getResumeId,
                CommonResumeRepository.ActiveRequestRow::getRequestId, (first, second) -> first));
        Set<Long> requestsWithComments = activeRequests.isEmpty() ? Set.of()
            : feedbackBoard.countComments(activeRequests.values()).stream()
                .filter(row -> row.getCount() > 0)
                .map(FeedbackBoardRepository.CommentCountRow::getRequestId)
                .collect(Collectors.toSet());
        return resumes.findByUserIdOrderByIdAsc(user.getId()).stream()
            .map(resume -> CommonResumeSummary.of(resume, feedbackCounts.getOrDefault(resume.getId(), 0L),
                targetCounts.getOrDefault(resume.getId(), 0L), activeRequests.get(resume.getId()),
                activeRequests.get(resume.getId()) != null
                    && requestsWithComments.contains(activeRequests.get(resume.getId()))))
            .toList();
    }

    @Override
    @Transactional
    public CommonResumeDetailResponse create(String username, CommonResumeRequest request) {
        var user = currentUser(username);
        if (request == null) throw new InvalidResumeException("이력서 정보가 없어요.");
        var resume = CommonResume.builder()
            .user(user)
            .title(validTitle(request.title()))
            .field(validField(request.field()))
            .photoUrl(media.prepareResumePhoto(request.photoUrl(), user.getId()))
            .content(request.content() == null ? new LinkedHashMap<>() : request.content())
            .build();
        return CommonResumeDetailResponse.from(resumes.save(resume));
    }

    // PATCH라서 보내지 않은(null) 값은 그대로 둔다. 분야를 빈 문자열로 보내면 분야를 지운다.
    @Override
    @Transactional
    public CommonResumeDetailResponse update(CommonResumeUpdateCommand command) {
        var user = currentUser(command.username());
        var resume = ownedResume(command.resumeId(), user);
        var request = command.request();
        if (request == null) throw new InvalidResumeException("이력서 정보가 없어요.");
        resume.update(
            request.title() == null ? resume.getTitle() : validTitle(request.title()),
            request.field() == null ? resume.getField() : validField(request.field()),
            request.content() == null ? resume.getContent() : request.content());
        if (request.photoUrl() != null) resume.updatePhoto(media.prepareResumePhoto(request.photoUrl(), user.getId()));
        resumes.flush();
        return CommonResumeDetailResponse.from(resume);
    }

    // 댓글이 없는 피드백 요청은 이력서와 함께 정리한다. 댓글이 있으면 기록을 보존한다.
    @Override
    @Transactional
    public void delete(Long resumeId, String username) {
        var user = users.findWithLockByUsername(username).orElseThrow(UnauthenticatedUserException::new);
        var resume = ownedResume(resumeId, user);
        var requests = feedbackRequests.findByCommonResume_IdOrderByIdAsc(resumeId);
        if (requests.stream().anyMatch(request -> feedbackComments.existsByFeedbackRequest_Id(request.getId()))) {
            throw new ResumeInUseException("피드백 댓글이 달린 이력서는 삭제할 수 없습니다.");
        }
        try {
            for (var request : requests) {
                feedbackSections.deleteByRequest_Id(request.getId());
                feedbackRequests.delete(request);
            }
            feedbackRequests.flush();
            if (user.getPinnedCommonResume() != null
                    && user.getPinnedCommonResume().getId().equals(resumeId)) {
                user.pinResume(null);
                users.flush();
            }
            applications.detachCommon(resumeId);
            targetResumes.detachCommonResume(resumeId);
            resumes.delete(resume);
            resumes.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResumeInUseException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResumeDetailResponse getMyResume(Long resumeId, String username) {
        var user = currentUser(username);
        return CommonResumeDetailResponse.from(ownedResume(resumeId, user));
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
