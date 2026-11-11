package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local user details service implementation.
 * 따라서 우리 폴더에는 이 서비스 구현체가 존재하고, 해당 계약서는 존재하지않는다.
 * 이 서비스는 UserDetailsService 인터페이스를 구현하며, 사용자 이름을 기반으로 사용자 세부 정보를 로드하는 기능을 제공.
 */
@Service
@RequiredArgsConstructor
public class LocalUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var user = users.findByUsername(username)
                .filter(member -> member.getPassword() != null && !member.getPassword().isBlank())
                .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호를 확인해 주세요."));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword()).roles(user.getRole()).build();
    }
}
