package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.SessionMemberResponse;
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

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionMemberResponse> findSessionMember(String username) {
        return users.findByUsername(username)
            .map(user -> new SessionMemberResponse(user.getId(), user.getUsername(), user.getNickname()));
    }
}
