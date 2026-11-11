package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.feedback.FeedbackCommentCreateCommand;
import com.newlecture.resuit.dto.notification.NotificationCommand;
import com.newlecture.resuit.dto.feedback.FeedbackCommentResponse;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import com.newlecture.resuit.dto.resume.ResumePreviewCondition;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import com.newlecture.resuit.entity.FeedbackComment;
import com.newlecture.resuit.entity.FeedbackRequest;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.BestReviewRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository;
import com.newlecture.resuit.repository.FeedbackCommentLikeRepository;
import com.newlecture.resuit.repository.FeedbackCommentRepository;
import com.newlecture.resuit.repository.FeedbackRequestRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository.CommentCountRow;
import com.newlecture.resuit.repository.FeedbackBoardRepository.SectionRow;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.FeedbackBoardService;
import com.newlecture.resuit.service.NotificationService;
import com.newlecture.resuit.service.NotificationType;
import com.newlecture.resuit.service.FeedbackBoardService.FeedbackCommentForbiddenException;
import com.newlecture.resuit.service.FeedbackBoardService.FeedbackCommentInUseException;
import com.newlecture.resuit.service.FeedbackBoardService.FeedbackCommentNotFoundException;
import com.newlecture.resuit.service.FeedbackRequestService.FeedbackRequestNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackBoardServiceImpl implements FeedbackBoardService {
    private static final Set<String> CATEGORIES = Set.of("FRONTEND", "BACKEND", "FULLSTACK");
    // docs/database-design.md 이력서 항목. ANSWERS는 특화 이력서의 요구 문항 답변
    private static final List<String> COMMON_SECTIONS = List.of("BASIC", "SKILLS", "PROJECTS", "AWARDS", "INTRODUCTION");
    private static final List<String> TARGET_SECTIONS = List.of("BASIC", "SKILLS", "PROJECTS", "AWARDS", "INTRODUCTION", "ANSWERS");

    private final FeedbackBoardRepository board;
    private final FeedbackCommentRepository comments;
    private final FeedbackCommentLikeRepository likes;
    private final BestReviewRepository bestReviews;
    private final FeedbackRequestRepository requests;
    private final CommonResumeRepository commonResumes;
    private final TargetResumeRepository targetResumes;
    private final UserRepository users;
    private final NotificationService notifications;

    // 4.1 전체, 4.3 분야 필터, 4.4 내 요청. 섹션과 댓글 수는 요청마다 따로 묻지 않고 한 번에 모아 온다.
    @Override
    @Transactional(readOnly = true)
    public List<FeedbackRequestSummary> getRequests(String username, FeedbackRequestSearchCondition condition) {
        var user = currentUser(username);
        var requests = board.findActive(validCategory(condition.category()),
            condition.mine() ? user.getUsername() : null);
        if (requests.isEmpty()) return List.of();

        var ids = requests.stream().map(FeedbackRequest::getId).toList();
        Map<Long, List<String>> sections = board.findSections(ids).stream()
            .collect(Collectors.groupingBy(SectionRow::getRequestId,
                Collectors.mapping(SectionRow::getSectionType, Collectors.toList())));
        Map<Long, CommentCountRow> counts = board.countComments(ids).stream()
            .collect(Collectors.toMap(CommentCountRow::getRequestId, Function.identity()));

        return requests.stream().map(request -> {
            var count = counts.get(request.getId());
            return FeedbackRequestSummary.of(request, sections.getOrDefault(request.getId(), List.of()),
                count == null ? 0 : count.getCount(),
                count != null && count.getAdoptedCount() > 0,
                request.getUser().getId().equals(user.getId()));
        }).toList();
    }

    // 피드백 게시글 상세. 목록 카드와 같은 요약을 한 건만 반환한다.
    @Override
    @Transactional(readOnly = true)
    public FeedbackRequestSummary getRequest(Long requestId, String username) {
        var user = currentUser(username);
        var request = visibleRequest(requestId, user);
        var ids = List.of(request.getId());
        var sections = board.findSections(ids).stream().map(SectionRow::getSectionType).toList();
        var count = board.countComments(ids).stream().findFirst().orElse(null);
        return FeedbackRequestSummary.of(request, sections,
            count == null ? 0 : count.getCount(),
            count != null && count.getAdoptedCount() > 0,
            request.getUser().getId().equals(user.getId()));
    }

    // 4.5 미리보기. 남의 이력서는 피드백 요청이 진행 중이거나 공개한 이력서일 때만 보여 준다.
    @Override
    @Transactional(readOnly = true)
    public ResumePreviewResponse getResumePreview(String username, ResumePreviewCondition condition) {
        var user = currentUser(username);
        var id = condition.id();
        var type = condition.type() == null ? "" : condition.type().trim().toUpperCase(Locale.ROOT);
        if (!type.equals("COMMON") && !type.equals("TARGET")) {
            throw new InvalidResumeException("이력서 종류(type)는 COMMON 또는 TARGET이어야 해요.");
        }
        if (id == null || id <= 0) throw new ResumeNotFoundException();

        if (type.equals("COMMON")) {
            var resume = commonResumes.findById(id).orElseThrow(ResumeNotFoundException::new);
            checkVisible(resume.getUser(), user, resume.isPublic() || commonResumes.hasActiveFeedbackRequest(id));
            return ResumePreviewResponse.from(resume);
        }
        var resume = targetResumes.findById(id).orElseThrow(ResumeNotFoundException::new);
        checkVisible(resume.getUser(), user, targetResumes.hasActiveFeedbackRequest(id));
        var preview = ResumePreviewResponse.of(resume, targetResumes.findQuestions(resume.getJob().getId()));
        // 특화 이력서는 어느 회사에 지원하는지 남에게 보이지 않게 회사 이름을 가린다
        return resume.getUser().getId().equals(user.getId()) ? preview : preview.withoutCompany();
    }

    // 4.6 댓글 목록. 오래된 순으로 보여 준다.
    @Override
    @Transactional(readOnly = true)
    public List<FeedbackCommentResponse> getComments(Long requestId, String username) {
        var user = currentUser(username);
        var request = visibleRequest(requestId, user);
        return board.findComments(request.getId()).stream()
            .map(comment -> FeedbackCommentResponse.of(comment, comment.getUser().getId().equals(user.getId())))
            .toList();
    }

    // 4.7 댓글 작성. 4.9에서 고른 섹션을 함께 저장한다.
    @Override
    @Transactional
    public FeedbackCommentResponse createComment(FeedbackCommentCreateCommand command) {
        var user = currentUser(command.username());
        if (command.requestId() == null || command.requestId() <= 0) throw new FeedbackRequestNotFoundException();
        var request = requests.findForAcceptance(command.requestId()).orElseThrow(FeedbackRequestNotFoundException::new);
        if (request.isCanceled() && !request.getUser().getId().equals(user.getId())) {
            throw new FeedbackRequestNotFoundException();
        }
        if (request.isCanceled()) throw new FeedbackRequestClosedException();
        var body = command.request();
        var saved = comments.save(FeedbackComment.builder()
            .feedbackRequest(request)
            .user(user)
            .section(validSection(body.section(), request))
            .content(body.content().trim())
            .build());
        // 피드백 요청자에게 피드백이 달렸다고 알린다.
        notifications.notify(new NotificationCommand(request.getUser().getId(), user.getId(), NotificationType.FEEDBACK_COMMENT,
            NotificationService.nameOf(user) + " 님이 내 이력서에 피드백을 남겼어요.", "/feedback/" + request.getId()));
        return FeedbackCommentResponse.of(saved, true);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        var user = currentUser(username);
        if (commentId == null || commentId <= 0) throw new FeedbackCommentNotFoundException();
        var requestId = comments.findRequestId(commentId).orElseThrow(FeedbackCommentNotFoundException::new);
        requests.findForAcceptance(requestId).orElseThrow(FeedbackCommentNotFoundException::new);
        var comment = comments.findById(commentId).orElseThrow(FeedbackCommentNotFoundException::new);
        if (!comment.getUser().getId().equals(user.getId())) throw new FeedbackCommentForbiddenException();
        if (comment.isAdopted() || bestReviews.existsByComment_CommentId(commentId)) {
            throw new FeedbackCommentInUseException();
        }
        var parentId = comment.getParent() == null ? null : comment.getParent().getCommentId();
        comments.reparentChildren(commentId, parentId);
        likes.deleteByCommentId(commentId);
        try {
            comments.delete(comment);
            comments.flush();
        } catch (DataIntegrityViolationException e) {
            throw new FeedbackCommentInUseException();
        }
    }

    // 취소한 요청은 게시판에서 내려가므로 요청한 본인만 계속 볼 수 있다.
    private FeedbackRequest visibleRequest(Long requestId, User user) {
        if (requestId == null || requestId <= 0) throw new FeedbackRequestNotFoundException();
        var request = board.findRequest(requestId).orElseThrow(FeedbackRequestNotFoundException::new);
        if (request.isCanceled() && !request.getUser().getId().equals(user.getId())) {
            throw new FeedbackRequestNotFoundException();
        }
        return request;
    }

    // 요청자가 고른 섹션 중 하나만 받는다. 고른 섹션이 없으면 그 이력서의 모든 항목을 받는다. 비우면 이력서 전체
    private String validSection(String section, FeedbackRequest request) {
        if (section == null || section.isBlank()) return null;
        var normalized = section.trim().toUpperCase(Locale.ROOT);
        var requested = board.findSections(List.of(request.getId())).stream().map(SectionRow::getSectionType).toList();
        var allowed = !requested.isEmpty() ? requested
            : request.getTargetResume() != null ? TARGET_SECTIONS : COMMON_SECTIONS;
        if (!allowed.contains(normalized)) throw new InvalidResumeException("피드백할 섹션 값이 올바르지 않아요: " + section);
        return normalized;
    }

    private static void checkVisible(User owner, User user, boolean shared) {
        if (!shared && !owner.getId().equals(user.getId())) throw new ResumeForbiddenException();
    }

    private User currentUser(String username) {
        if (username == null || username.isBlank()) throw new UnauthenticatedUserException();
        return users.findByUsername(username).orElseThrow(UnauthenticatedUserException::new);
    }

    private static String validCategory(String category) {
        if (category == null || category.isBlank()) return null;
        var normalized = category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(normalized)) throw new InvalidResumeException("분야는 FRONTEND, BACKEND, FULLSTACK 중 하나여야 해요.");
        return normalized;
    }
}
