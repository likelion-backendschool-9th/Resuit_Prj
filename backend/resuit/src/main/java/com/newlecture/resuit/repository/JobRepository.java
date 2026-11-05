package com.newlecture.resuit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
	Page<Job> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	Page<Job> findByTypeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(
			String type,
			Pageable pageable);
}
