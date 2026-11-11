package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.SessionMemberResponse;
import com.newlecture.resuit.dto.SignupRequest;
import java.util.Optional;
import com.newlecture.resuit.dto.AuthAccountResponse;
import com.newlecture.resuit.dto.PasswordSetupRequest;
import com.newlecture.resuit.dto.LoginRequest;
import org.springframework.security.core.Authentication;

public interface AuthService {
    Authentication authenticate(LoginRequest request);
    Optional<AuthAccountResponse> findAccount(String username);
    AuthAccountResponse setupPassword(String username, PasswordSetupRequest request);
    void validateSocialLink(String username, String provider);
    void unlinkSocialAccount(String username, String provider);

    SessionMemberResponse signup(SignupRequest request);

    Optional<SessionMemberResponse> findSessionMember(String username);
}
