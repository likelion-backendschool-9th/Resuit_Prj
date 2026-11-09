package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.SessionMemberResponse;
import com.newlecture.resuit.dto.AuthAccountResponse;
import com.newlecture.resuit.dto.PasswordSetupRequest;
import com.newlecture.resuit.dto.LoginRequest;
import com.newlecture.resuit.repository.SocialAccountRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.newlecture.resuit.dto.SignupRequest;
import com.newlecture.resuit.entity.User;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.AuthService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AuthenticationManager authenticationManager;
    private final SocialAccountRepository socialAccounts;

    @Override
    public Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthAccountResponse> findAccount(String username) {
        return users.findByUsername(username).map(this::accountResponse);
    }

    @Override
    @Transactional
    public AuthAccountResponse setupPassword(String username, PasswordSetupRequest request) {
        User user = lockedUser(username);
        if (user.hasPasswordLogin()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 비밀번호 로그인이 설정되어 있습니다.");
        }
        if (socialAccounts.countByUser_Id(user.getId()) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "연결된 소셜 로그인 계정이 필요합니다.");
        }
        validatePassword(request.password());
        user.enablePasswordLogin(passwords.encode(request.password()));
        return accountResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateSocialLink(String username, String provider) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (socialAccounts.findByUser_IdAndProvider(user.getId(), provider).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 연결된 제공자입니다.");
        }
    }

    @Override
    @Transactional
    public void unlinkSocialAccount(String username, String provider) {
        User user = lockedUser(username);
        var account = socialAccounts.findByUser_IdAndProvider(user.getId(), provider)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "연결된 제공자가 없습니다."));
        if (!user.hasPasswordLogin() && socialAccounts.countByUser_Id(user.getId()) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마지막 로그인 수단은 해제할 수 없습니다.");
        }
        socialAccounts.delete(account);
    }

    private User lockedUser(String username) {
        return users.findWithLockByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private AuthAccountResponse accountResponse(User user) {
        var linked = socialAccounts.findByUser_Id(user.getId()).stream()
                .map(account -> account.getProvider()).sorted().toList();
        return new AuthAccountResponse(
                new SessionMemberResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole()),
                linked, user.hasPasswordLogin());
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상, UTF-8 기준 72바이트 이하여야 합니다.");
        }
    }


    @Override
    @Transactional
    public SessionMemberResponse signup(SignupRequest request) {
        validatePassword(request.password());
        if (users.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        
        var user = User.builder().username(request.username()).email(request.email())
                .password(passwords.encode(request.password())).nickname(request.nickname())
                .role("USER").subscription(false).build();
        try {
            // Flush here so concurrent duplicate signups also return 409.
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "회원 정보를 저장할 수 없습니다. 아이디 중복을 확인해 주세요.", exception);
        }
        return new SessionMemberResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<SessionMemberResponse> findSessionMember(String username) {
        return users.findByUsername(username)
            .map(user -> new SessionMemberResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole()));
    }
}
