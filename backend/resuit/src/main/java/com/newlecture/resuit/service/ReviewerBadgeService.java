package com.newlecture.resuit.service;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReviewerBadgeService {
    /** 분기 첫날 전체 회원을 평가한다. 여러 서버에서 실행할 때는 작업 잠금이 필요하다. */
    void evaluateQuarter(LocalDateTime at);

    /**
     * 피드백 채택 또는 베스트 리뷰 선정 직후 호출한다.
     * 유예(GRACE) 중 자격 회복만 즉시 반영하고, EXPIRED는 중도에 되살리지 않는다.
     */
    void refreshAfterActivity(UUID userId, LocalDateTime at);
}
