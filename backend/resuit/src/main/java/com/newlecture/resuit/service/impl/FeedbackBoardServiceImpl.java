package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import com.newlecture.resuit.dto.resume.ResumePreviewCondition;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import com.newlecture.resuit.entity.FeedbackRequest;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository.CommentCountRow;
import com.newlecture.resuit.repository.FeedbackBoardRepository.SectionRow;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
import com.newlecture.resuit.service.CommonResumeService.ResumeForbiddenException;
import com.newlecture.resuit.service.CommonResumeService.ResumeNotFoundException;
import com.newlecture.resuit.service.CommonResumeService.UnauthenticatedUserException;
import com.newlecture.resuit.service.FeedbackBoardService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackBoardServiceImpl implements FeedbackBoardService {
    private static final Set<String> CATEGORIES = Set.of("FRONTEND", "BACKEND");

    private final FeedbackBoardRepository board;
    private final CommonResumeRepository commonResumes;
    private final TargetResumeRepository targetResumes;
    private final UserRepository users;

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
        return ResumePreviewResponse.of(resume, targetResumes.findQuestions(resume.getJob().getId()));
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
        if (!CATEGORIES.contains(normalized)) throw new InvalidResumeException("분야는 FRONTEND 또는 BACKEND여야 해요.");
        return normalized;
    }
}
