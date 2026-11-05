package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import com.newlecture.resuit.entity.FeedbackRequest;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.FeedbackBoardRepository;
import com.newlecture.resuit.repository.FeedbackBoardRepository.CommentCountRow;
import com.newlecture.resuit.repository.FeedbackBoardRepository.SectionRow;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CommonResumeService.InvalidResumeException;
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
