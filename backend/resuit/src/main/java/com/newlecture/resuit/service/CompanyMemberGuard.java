package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// 판별은 여기 한 곳에만 둔다. 기준이 바뀌면 여기만 고치면 된다.
@Component
public class CompanyMemberGuard {

    private static final String COMPANY = "COMPANY";

    private final UserRepository users;

    public CompanyMemberGuard(UserRepository users) {
        this.users = users;
    }

    public User requireCompany(String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!COMPANY.equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user;
    }
}
