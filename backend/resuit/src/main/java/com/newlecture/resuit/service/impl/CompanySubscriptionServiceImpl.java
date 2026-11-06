package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.entity.CompanySubscription;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CompanySubscriptionRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanySubscriptionService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanySubscriptionServiceImpl implements CompanySubscriptionService {

    private static final String FREE = "FREE";
    private static final String PRO = "PRO";
    private static final int TRIAL_DAYS = 14;

    private final CompanySubscriptionRepository subscriptions;
    private final CompanyMemberGuard companyGuard;

    public CompanySubscriptionServiceImpl(CompanySubscriptionRepository subscriptions, CompanyMemberGuard companyGuard) {
        this.subscriptions = subscriptions;
        this.companyGuard = companyGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySubscriptionResponse getMySubscription(String username) {
        User company = companyGuard.requireCompany(username);
        return toResponse(subscriptions.findById(company.getId()).orElse(null));
    }

    @Override
    @Transactional
    public CompanySubscriptionResponse updateMySubscription(String username, CompanySubscriptionRequest request) {
        User company = companyGuard.requireCompany(username);
        // 이미 요금제 행이 있으면 체험을 했거나 유료 이용 중이라 다시 체험할 수 없다. 409로 끝낸다.
        if (subscriptions.existsById(company.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pro 체험은 기업당 한 번만 할 수 있습니다.");
        }
        CompanySubscription subscription = subscriptions.save(CompanySubscription.builder()
                .company(company)
                .plan(PRO)
                .trialEndsAt(now().plusDays(TRIAL_DAYS))
                .build());
        return toResponse(subscription);
    }

    // PRO이고 체험·이용 기간이 남아 있을 때만 Pro다(설계 문서 기준).
    @Override
    @Transactional(readOnly = true)
    public boolean isPro(User company) {
        return subscriptions.findById(company.getId()).map(this::isActivePro).orElse(false);
    }

    private boolean isActivePro(CompanySubscription subscription) {
        LocalDateTime now = now();
        return PRO.equals(subscription.getPlan())
                && (subscription.getTrialEndsAt() == null || subscription.getTrialEndsAt().isAfter(now))
                && (subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(now));
    }

    private CompanySubscriptionResponse toResponse(CompanySubscription subscription) {
        if (subscription == null) {
            return new CompanySubscriptionResponse(FREE, false, false, null, null, true);
        }
        boolean pro = isActivePro(subscription);
        boolean trial = pro && subscription.getTrialEndsAt() != null;
        // 체험이 끝났으면 plan은 FREE로 보여 준다. 행이 있으므로 다시 체험할 수는 없다.
        return new CompanySubscriptionResponse(pro ? PRO : FREE, pro, trial,
                subscription.getTrialEndsAt(), subscription.getExpiresAt(), false);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
