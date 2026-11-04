package com.newlecture.resuit.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
}
