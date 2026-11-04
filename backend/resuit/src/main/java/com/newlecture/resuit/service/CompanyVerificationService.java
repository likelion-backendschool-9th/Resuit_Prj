package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanyVerificationDto.*;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.entity.Verification;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.VerificationRepository;
import static com.newlecture.resuit.service.CompanyVerificationException.Kind.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyVerificationService {
    private static final String TYPE = "company_email";
    private final UserRepository users;
    private final VerificationRepository verifications;
    private final ObjectProvider<JavaMailSender> mail;
    private final String secret;
    private final String from;
    private final SecureRandom random = new SecureRandom();

    public CompanyVerificationService(UserRepository users, VerificationRepository verifications,
            ObjectProvider<JavaMailSender> mail,
            @Value("${resuit.verification.code-secret:}") String secret,
            @Value("${resuit.verification.mail-from:}") String from) {
        this.users = users; this.verifications = verifications; this.mail = mail;
        this.secret = secret; this.from = from;
    }

    @Transactional
    public EmailResponse send(String username, EmailRequest input) {
        var user = authenticatedUser(username);
        String email = normalizeEmail(input == null ? null : input.email());
        if (secret.length() < 32 || from.isBlank() || mail.getIfAvailable() == null) {
            throw new CompanyVerificationException(NOT_CONFIGURED, "이메일 인증 서비스를 준비 중입니다.");
        }
        var now = now();
        var previous = verifications.findFirstByUserIdAndTypeOrderByIdDesc(user.getId(), TYPE);
        if (previous.isPresent() && previous.get().getCreatedAt().plusSeconds(60).isAfter(now)) {
            throw new CompanyVerificationException(RESEND_TOO_SOON, "인증 메일은 60초 후 다시 요청해 주세요.");
        }
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        String salt = UUID.randomUUID().toString();
        var verification = Verification.builder().user(user).type(TYPE).email(email)
            .codeHash(salt + ":" + digest(salt, code)).expiresAt(now.plusMinutes(5)).build();
        var message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(email);
        message.setSubject("[Resuit] 기업 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n5분 안에 입력해 주세요. 요청하지 않았다면 이 메일을 무시해 주세요.");
        try { mail.getObject().send(message); }
        catch (MailException ex) {
            throw new CompanyVerificationException(DELIVERY_FAILED, "메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        previous.filter(v -> v.getVerifiedAt() == null).ifPresent(v -> v.expire(now));
        verifications.saveAndFlush(verification);
        return new EmailResponse(verification.getId(), verification.getExpiresAt(), 60);
    }

    @Transactional(noRollbackFor = CompanyVerificationException.class)
    public ConfirmResponse confirm(String username, ConfirmRequest input) {
        var user = authenticatedUser(username);
        if (input == null || input.verificationId() == null || input.verificationId() <= 0
                || input.code() == null || !input.code().matches("[0-9]{6}")) {
            throw new CompanyVerificationException(INVALID_INPUT, "인증 요청과 6자리 숫자 코드를 확인해 주세요.");
        }
        var verification = verifications.findById(input.verificationId())
            .filter(v -> TYPE.equals(v.getType()) && user.getId().equals(v.getUser().getId()))
            .orElseThrow(() -> new CompanyVerificationException(NOT_FOUND, "인증 요청을 찾을 수 없습니다."));
        if (verification.getVerifiedAt() != null) throw new CompanyVerificationException(ALREADY_VERIFIED, "이미 완료된 인증 요청입니다.");
        if (!verification.getExpiresAt().isAfter(now())) throw new CompanyVerificationException(EXPIRED, "인증 코드가 만료되었습니다. 다시 요청해 주세요.");
        if (verification.getFailedAttempts() >= 5) throw new CompanyVerificationException(ATTEMPTS_EXCEEDED, "입력 횟수를 초과했습니다. 인증 메일을 다시 요청해 주세요.");
        String[] stored = verification.getCodeHash().split(":", 2);
        if (stored.length != 2 || !MessageDigest.isEqual(stored[1].getBytes(StandardCharsets.UTF_8),
                digest(stored[0], input.code()).getBytes(StandardCharsets.UTF_8))) {
            verification.recordFailure();
            throw new CompanyVerificationException(CODE_MISMATCH, "인증 코드가 일치하지 않습니다.");
        }
        verification.verify(now());
        return new ConfirmResponse(verification.getId(), verification.getEmail(), verification.getVerifiedAt());
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

    private String digest(String salt, String code) {
        if (secret.length() < 32) throw new CompanyVerificationException(NOT_CONFIGURED, "이메일 인증 서비스를 준비 중입니다.");
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal((salt + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) { throw new IllegalStateException("Cannot hash verification code", ex); }
    }

    private LocalDateTime now() { return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS); }
}
