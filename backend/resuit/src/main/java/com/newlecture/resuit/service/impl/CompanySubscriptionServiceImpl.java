package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.entity.CompanyPlanPeriod;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CompanyPlanPeriodRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanySubscriptionService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 기업 요금제를 조회하고 Pro 체험을 시작한다. 요금제 상태는 company_plan_periods의 PAID 줄(체험·결제)로 계산한다.
// 이용 기간 줄이 없는 기업은 FREE다. 체험은 TRIAL 줄(14일)이며, 체험이나 결제 기록이 있으면 다시 체험할 수 없다.
@Service
public class CompanySubscriptionServiceImpl implements CompanySubscriptionService {

    private static final String FREE = "FREE";
    private static final String PRO = "PRO";
    private static final int TRIAL_DAYS = 14;
    private static final String PAID = "PAID";

    private final CompanyPlanPeriodRepository periods;
    // 같은 회사의 체험·결제를 한 번에 하나씩 처리하려고 회원 행을 잠글 때 쓴다.
    private final UserRepository users;
    private final CompanyMemberGuard companyGuard;

    public CompanySubscriptionServiceImpl(CompanyPlanPeriodRepository periods, UserRepository users,
            CompanyMemberGuard companyGuard) {
        this.periods = periods;
        this.users = users;
        this.companyGuard = companyGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySubscriptionResponse getMySubscription(String username) {
        User company = companyGuard.requireCompany(username);
        return toResponse(paidPeriods(company));
    }

    @Override
    @Transactional
    public CompanySubscriptionResponse updateMySubscription(String username, CompanySubscriptionRequest request) {
        User company = companyGuard.requireCompany(username);
        // 같은 회사가 동시에 체험을 두 번 요청해도 한 줄만 생기도록 회원 행을 잠근다.
        lockCompany(company);
        // 체험했거나 결제한 적이 있으면 다시 체험할 수 없다. 409로 끝낸다.
        if (!paidPeriods(company).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pro 체험은 기업당 한 번만 할 수 있습니다.");
        }
        periods.save(CompanyPlanPeriod.trial(company, TRIAL_DAYS, now()));
        return toResponse(paidPeriods(company));
    }

    // 결제한 Pro가 남아 있으면 가장 늦은 끝 시각 뒤로 이어 붙이고, 없으면(무료·체험 중·만료) 지금부터 시작한다.
    // 체험 중에 결제하면 결제 기간이 지금 시작되므로 체험은 끝난 것으로 본다.
    @Override
    @Transactional
    public CompanySubscriptionResponse activatePaid(User company, CompanyPlanPeriod period, String paymentKey) {
        // 같은 회사의 결제 두 건이 동시에 승인돼도 기간이 겹치지 않도록 회원 행을 잠근다.
        lockCompany(company);
        LocalDateTime now = now();
        LocalDateTime start = paidPeriods(company).stream()
                .filter(p -> !p.isTrial())
                .map(CompanyPlanPeriod::getEndsAt)
                .filter(end -> end.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(now);
        period.markPaid(paymentKey, start);
        return toResponse(paidPeriods(company));
    }

    // 지금 Pro 기능을 쓸 수 있는지(체험 중이거나 결제한 기간 안). 인재 찾기 상세 필터에서 쓴다.
    @Override
    @Transactional(readOnly = true)
    public boolean isPro(User company) {
        return toResponse(paidPeriods(company)).pro();
    }

    private List<CompanyPlanPeriod> paidPeriods(User company) {
        return periods.findByCompany_IdAndStatus(company.getId(), PAID);
    }

    // PAID 줄들로 지금 요금제를 계산한다. 화면이 받는 응답 모양은 요금제 테이블을 쓰던 때와 같다.
    private CompanySubscriptionResponse toResponse(List<CompanyPlanPeriod> rows) {
        if (rows.isEmpty()) {
            return new CompanySubscriptionResponse(FREE, false, false, null, null, true);
        }
        LocalDateTime now = now();
        List<CompanyPlanPeriod> paid = rows.stream().filter(p -> !p.isTrial()).toList();
        CompanyPlanPeriod trialRow = rows.stream().filter(CompanyPlanPeriod::isTrial).findFirst().orElse(null);

        boolean paidActive = paid.stream().anyMatch(p -> !p.getStartsAt().isAfter(now) && p.getEndsAt().isAfter(now));
        // 결제 기간이 이미 시작됐으면 체험은 끝난 것으로 본다.
        boolean paidStarted = paid.stream().anyMatch(p -> !p.getStartsAt().isAfter(now));
        boolean trial = trialRow != null && trialRow.getEndsAt().isAfter(now) && !paidStarted;
        boolean pro = paidActive || trial;
        LocalDateTime expiresAt = paid.stream().map(CompanyPlanPeriod::getEndsAt)
                .filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
        return new CompanySubscriptionResponse(pro ? PRO : FREE, pro, trial,
                trial ? trialRow.getEndsAt() : null, expiresAt, false);
    }

    // 회원 행을 잠근다. 트랜잭션이 끝날 때까지 같은 회사의 다른 체험·결제 처리는 기다린다.
    private void lockCompany(User company) {
        users.findWithLockById(company.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
