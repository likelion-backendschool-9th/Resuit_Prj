package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.AdoptFeedbackResponse;
import java.util.UUID;

/** 이력서 피드백 채택 API용 어댑터. 실제 채택·배지 갱신은 {@link ReviewService}를 사용한다. */
public interface FeedbackAdoptionService {
    AdoptFeedbackResponse adopt(Long commentId, UUID requesterId);
    AdoptFeedbackResponse adoptByUsername(Long commentId, String usernameHeader);
}
