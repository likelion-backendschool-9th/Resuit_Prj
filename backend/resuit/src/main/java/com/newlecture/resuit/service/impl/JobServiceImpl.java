package com.newlecture.resuit.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.repository.UserRepository;
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
	private final JobApplicationRepository jobApplicationRepository;
	private final CommonResumeRepository commonResumeRepository;
	private final UserRepository userRepository;

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

	@Override
	@Transactional
	public JobApplicationResponse applyForGeneralJob(Long jobId, GeneralJobApplicationRequest request, String username) {
		
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobId));

		if (!"GENERAL".equalsIgnoreCase(job.getType())) {
			throw new IllegalArgumentException("일반 공고만 일반 지원으로 신청할 수 있습니다.");
		}
		if (jobApplicationRepository.existsByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())) {
			throw new IllegalArgumentException("이미 지원한 공고입니다.");
		}

		var commonResume = commonResumeRepository.findByIdAndUserId(
				request.commonResumeId(), user.getId())
				.orElseThrow(() -> new IllegalArgumentException("본인의 공통 이력서를 찾을 수 없습니다."));
		var application = JobApplication.builder()
				.job(job)
				.user(user)
				.commonResume(commonResume)
				.status("APPLIED")
				.build();
		return JobApplicationResponse.from(jobApplicationRepository.save(application));
	}
}
