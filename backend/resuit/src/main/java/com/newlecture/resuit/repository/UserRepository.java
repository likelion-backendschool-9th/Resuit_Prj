package com.newlecture.resuit.repository;

import java.util.Collection;

import com.newlecture.resuit.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByIdAndProfileImageUrlIn(UUID ownerId, Collection<String> paths);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // 지원과 지원 취소를 같은 회원 행의 잠금으로 직렬화한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findWithLockByUsername(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findForVerification(@Param("username") String username);

    // 같은 회원의 크레딧 변경이 한 줄로 처리되도록 회원 행을 잠근다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findWithLockById(UUID id);
}
