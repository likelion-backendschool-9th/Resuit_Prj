package com.newlecture.resuit.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Mentor;

public interface MentorRepository extends JpaRepository<Mentor, UUID> {

	@EntityGraph(attributePaths = "user")
	Page<Mentor> findAllByOrderByVerifiedAtDescUserIdDesc(Pageable pageable);
}
