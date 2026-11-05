package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.entity.Job;
import com.newlecture.resuit.repository.JobRepository;
import com.newlecture.resuit.repository.JobQuestionRepository;
import com.newlecture.resuit.service.JobService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

	private final JobRepository jobRepository;
	private final JobQuestionRepository jobQuestionRepository;

	@Override
	public Page<JobResponse> getJobs(String type, Pageable pageable) {
		Page<Job> jobs = type == null || type.isBlank()
				? jobRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
				: jobRepository.findByTypeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtDesc(type, pageable);
		return jobs.map(JobResponse::from);
	}

	@Override
	public JobDetailResponse getJob(Long id) {
		var job = jobRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + id));
		var questions = jobQuestionRepository.findByJob_IdOrderByOrderNoAsc(id).stream()
				.map(question -> new JobDetailResponse.Question(
						question.getId(),
						question.getContent(),
						question.getCharLimit(),
						question.getOrderNo()))
				.toList();
		return JobDetailResponse.from(job, questions);
	}
}
