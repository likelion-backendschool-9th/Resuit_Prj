package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.entity.User;

public interface CompanySubscriptionService {

    CompanySubscriptionResponse getMySubscription(String username);

    CompanySubscriptionResponse updateMySubscription(String username, CompanySubscriptionRequest request);

    boolean isPro(User company);

    // Pro 결제가 승인되면 이용 기간을 days일 늘린다. 결제 승인과 같은 트랜잭션에서 불린다.
    CompanySubscriptionResponse extendPaidPro(User company, int days);
}
