package com.newlecture.resuit.service.impl;

import static com.newlecture.resuit.service.CompanyVerificationException.Kind.*;

import com.newlecture.resuit.dto.CompanyVerificationDto.*;
import com.newlecture.resuit.entity.CompanyEmailDomain;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.Verification;
import com.newlecture.resuit.repository.CompanyEmailDomainRepository;
import com.newlecture.resuit.repository.MentorRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.VerificationRepository;
import com.newlecture.resuit.service.CompanyVerificationException;
import com.newlecture.resuit.service.CompanyVerificationService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyVerificationServiceImpl implements CompanyVerificationService {
    private static final String TYPE = "company_email";
    // 메일 코드 없이 바로 인증하므로 저장할 코드가 없다. code_hash 컬럼이 NOT NULL이라 이 값을 넣는다.
    private static final String NO_CODE = "NONE";
    private final UserRepository users;
    private final VerificationRepository verifications;
    private final CompanyEmailDomainRepository companyDomains;
    // 현직자 정보. 다른 회사로 다시 인증하면 회사명을 바꾼다.
    private final MentorRepository mentors;

    public CompanyVerificationServiceImpl(UserRepository users, VerificationRepository verifications,
            CompanyEmailDomainRepository companyDomains, MentorRepository mentors) {
        this.users = users;
        this.verifications = verifications;
        this.companyDomains = companyDomains;
        this.mentors = mentors;
    }

    @Override
    @Transactional
    public EmailResponse verify(String username, EmailRequest input) {
        var user = authenticatedUser(username);
        String email = normalizeEmail(input == null ? null : input.email());
        String domain = email.substring(email.indexOf('@') + 1);
        var companyDomain = companyDomains.findByDomain(domain)
            .orElseThrow(() -> new CompanyVerificationException(DOMAIN_NOT_ALLOWED, "현직자 인증에 사용할 수 없는 이메일입니다. 회사 이메일을 입력해 주세요."));
        var now = now();
        // 현직자 인증은 회사 하나만 유효하다. 다른 회사 이메일로 다시 인증하면(이직) 이전 인증을 취소한다.
        verifications.findByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNull(user.getId(), TYPE)
            .forEach(previous -> previous.revoke(now));
        // 인증 완료 기록을 남긴다. 내 인증 상태 조회(/verifications/me)가 이 기록을 읽는다.
        var verification = Verification.builder().user(user).type(TYPE).email(email)
            .codeHash(NO_CODE).expiresAt(now).verifiedAt(now).build();
        verifications.save(verification);
        // 현직자 인증만 하고 회원 역할(role)은 바꾸지 않는다. 기업 회원은 회원가입에서 따로 만든다.
        // 이미 현직자 정보가 있으면 새 회사로 바꾼다. 처음이면 현직자 정보 저장(PUT /users/me/mentor) 때 만든다.
        mentors.findById(user.getId())
            .ifPresent(mentor -> mentor.changeCompany(companyName(companyDomain), now));
        return new EmailResponse(verification.getId(), verification.getEmail(), verification.getVerifiedAt());
    }

    @Override
    @Transactional
    public MyStatusResponse cancel(String username) {
        var user = authenticatedUser(username);
        var active = verifications.findByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNull(user.getId(), TYPE);
        if (!active.isEmpty()) {
            var revokedAt = now();
            // 인증을 취소하면 현직자 목록·상세·유료 피드백 요청에서 빠진다. 현직자 행은 지난 요청 기록 때문에 남겨 둔다.
            active.forEach(verification -> verification.revoke(revokedAt));
        }
        return new MyStatusResponse(TYPE, "NONE", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public MyStatusResponse getMyStatus(UUID userId) {
        return verifications
                .findFirstByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNullOrderByVerifiedAtDesc(userId, TYPE)
                .map(v -> new MyStatusResponse(TYPE, "VERIFIED", v.getEmail(), v.getVerifiedAt()))
                .orElseGet(() -> new MyStatusResponse(TYPE, "NONE", null, null));
    }

    private User authenticatedUser(String username) {
        if (username == null || username.isBlank()) throw new CompanyVerificationException(UNAUTHENTICATED, "로그인이 필요합니다.");
        return users.findForVerification(username)
            .orElseThrow(() -> new CompanyVerificationException(UNAUTHENTICATED, "로그인이 필요합니다."));
    }

    private String normalizeEmail(String input) {
        if (input == null || input.length() > 255) throw new CompanyVerificationException(INVALID_INPUT, "이메일 주소를 확인해 주세요.");
        String email = input.trim();
        if (!email.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+")) {
            throw new CompanyVerificationException(INVALID_INPUT, "이메일 주소를 확인해 주세요.");
        }
        int at = email.indexOf('@');
        return email.substring(0, at) + "@" + email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    // 도메인의 회사명. 등록하지 않았으면 도메인을 그대로 쓴다.
    static String companyName(CompanyEmailDomain domain) {
        return domain.getCompanyName() == null || domain.getCompanyName().isBlank()
            ? domain.getDomain() : domain.getCompanyName();
    }

    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
