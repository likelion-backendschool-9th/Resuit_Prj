package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.AdoptFeedbackResponse;
import com.newlecture.resuit.dto.ReviewAcceptResponse;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CurrentUserResolver;
import com.newlecture.resuit.service.FeedbackAdoptionService;
import com.newlecture.resuit.service.ReviewService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackAdoptionServiceImpl implements FeedbackAdoptionService {
    private final ReviewService reviews;
    private final CurrentUserResolver currentUser;
    private final UserRepository users;

    @Override
    @Transactional
    public AdoptFeedbackResponse adopt(Long commentId, UUID requesterId) {
        String username = users.findById(requesterId)
                .orElseThrow(ReviewService.UnauthenticatedUserException::new)
                .getUsername();
        ReviewAcceptResponse accepted = reviews.acceptReview(commentId, username);
        return new AdoptFeedbackResponse(accepted.reviewId(), accepted.adopted(), accepted.adoptedAt());
    }

    @Override
    @Transactional
    public AdoptFeedbackResponse adoptByUsername(Long commentId, String usernameHeader) {
        return adopt(commentId, currentUser.requireUserId(usernameHeader));
    }
}
