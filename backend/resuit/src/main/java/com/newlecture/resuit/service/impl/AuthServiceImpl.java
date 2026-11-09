package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.SessionMemberResponse;
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

    @Override
    @Transactional
    public SessionMemberResponse signup(SignupRequest request) {
        // BCrypt's limit is in bytes, not Java characters.
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
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
