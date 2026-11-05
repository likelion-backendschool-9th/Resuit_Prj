package com.newlecture.resuit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.JobQuestion;

public interface JobQuestionRepository extends JpaRepository<JobQuestion, Long> {
	List<JobQuestion> findByJob_IdOrderByOrderNoAsc(Long jobId);
}
