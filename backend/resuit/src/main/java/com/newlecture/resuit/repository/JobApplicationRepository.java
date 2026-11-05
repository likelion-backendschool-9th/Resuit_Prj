package com.newlecture.resuit.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
	boolean existsByJobIdAndUserIdAndCanceledAtIsNull(Long jobId, UUID userId);
}
