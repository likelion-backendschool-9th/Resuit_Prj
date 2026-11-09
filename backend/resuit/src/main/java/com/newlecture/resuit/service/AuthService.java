package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.SessionMemberResponse;
import com.newlecture.resuit.dto.SignupRequest;
import java.util.Optional;

public interface AuthService {
    SessionMemberResponse signup(SignupRequest request);

    Optional<SessionMemberResponse> findSessionMember(String username);
}
