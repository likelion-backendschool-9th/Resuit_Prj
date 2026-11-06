package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.SessionMemberResponse;
import java.util.Optional;

public interface AuthService {
    Optional<SessionMemberResponse> findSessionMember(String username);
}
