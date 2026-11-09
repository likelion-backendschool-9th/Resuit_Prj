package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.entity.User;

public interface CompanySubscriptionService {

    CompanySubscriptionResponse getMySubscription(String username);

    CompanySubscriptionResponse updateMySubscription(String username, CompanySubscriptionRequest request);

    boolean isPro(User company);
}
