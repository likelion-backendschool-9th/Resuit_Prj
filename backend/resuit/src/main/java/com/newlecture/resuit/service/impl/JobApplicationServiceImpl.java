package com.newlecture.resuit.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;
import com.newlecture.resuit.dto.SpecialJobApplicationRequest;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.repository.JobRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.JobApplicationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CommonResumeRepository commonResumeRepository;
    private final TargetResumeRepository targetResumeRepository;
    private final UserRepository userRepository;

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

	@Override
	@Transactional
	public JobApplicationResponse applyForSpecialJob(
			Long jobId,
			SpecialJobApplicationRequest request,
			String username) {
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobId));

		if (!"SPECIAL".equalsIgnoreCase(job.getType())) {
			throw new IllegalArgumentException("특화 공고만 특화 지원으로 신청할 수 있습니다.");
		}
		if (jobApplicationRepository.existsByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())) {
			throw new IllegalArgumentException("이미 지원한 공고입니다.");
		}

		var targetResume = targetResumeRepository.findById(request.targetResumeId())
				.orElseThrow(() -> new IllegalArgumentException("특화 이력서를 찾을 수 없습니다."));
		if (!targetResume.getUser().getId().equals(user.getId())) {
			throw new IllegalArgumentException("본인의 특화 이력서만 제출할 수 있습니다.");
		}
		if (!targetResume.getJob().getId().equals(jobId)) {
			throw new IllegalArgumentException("해당 공고용 특화 이력서가 아닙니다.");
		}

		var application = JobApplication.builder()
				.job(job)
				.user(user)
				.targetResume(targetResume)
				.status("APPLIED")
				.build();
		return JobApplicationResponse.from(jobApplicationRepository.save(application));
	}

	@Override
	@Transactional
	public void cancelApplication(Long jobId, String username) {
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var application = jobApplicationRepository.findByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("취소할 지원 내역이 없거나 이미 취소한 지원입니다."));
		application.cancel();
	}
}
