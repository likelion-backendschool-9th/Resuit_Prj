package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.entity.CompanyPlanPeriod;
import com.newlecture.resuit.entity.User;

public interface CompanySubscriptionService {

    CompanySubscriptionResponse getMySubscription(String username);

    CompanySubscriptionResponse updateMySubscription(String username, CompanySubscriptionRequest request);

    // 이 기업 회원이 지금 Pro 기능을 쓸 수 있는지 확인한다. 인재 찾기 상세 필터에서 쓴다.
    boolean isPro(User company);

    // Pro 결제가 승인되면 이용 기간을 정해 PAID로 바꾼다. 결제 승인과 같은 트랜잭션에서 불린다.
    CompanySubscriptionResponse activatePaid(User company, CompanyPlanPeriod period, String paymentKey);
}
