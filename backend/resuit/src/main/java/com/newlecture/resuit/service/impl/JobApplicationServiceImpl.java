package com.newlecture.resuit.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.JobApplicationCommand;
import com.newlecture.resuit.dto.JobApplicationCommonResponse;
import com.newlecture.resuit.dto.JobApplicationTargetResponse;
import com.newlecture.resuit.dto.MyJobApplicationResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.entity.Job;
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
	public JobApplicationCommonResponse applyForGeneralJob(JobApplicationCommand command) {
        Long jobId = command.jobId();
        String username = command.username();

		var user = userRepository.findWithLockByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobId));

        validateAcceptingApplications(job);
		if (!"GENERAL".equalsIgnoreCase(job.getType())) {
			throw new IllegalArgumentException("일반 공고만 일반 지원으로 신청할 수 있습니다.");
		}
		if (jobApplicationRepository.existsByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())) {
			throw new IllegalArgumentException("이미 지원한 공고입니다.");
		}

		var commonResume = commonResumeRepository.findByIdAndUserId(
				command.resumeId(), user.getId())
				.orElseThrow(() -> new IllegalArgumentException("본인의 공통 이력서를 찾을 수 없습니다."));
		var application = JobApplication.builder()
				.job(job)
				.user(user)
				.commonResume(commonResume)
				.status("APPLIED")
				.build();
		return JobApplicationCommonResponse.from(jobApplicationRepository.save(application));
	}

	@Override
	@Transactional
	public JobApplicationTargetResponse applyForSpecialJob(JobApplicationCommand command) {
        Long jobId = command.jobId();
        String username = command.username();
		var user = userRepository.findWithLockByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobId));

        validateAcceptingApplications(job);
		if (!"SPECIAL".equalsIgnoreCase(job.getType())) {
			throw new IllegalArgumentException("특화 공고만 특화 지원으로 신청할 수 있습니다.");
		}
		if (jobApplicationRepository.existsByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())) {
			throw new IllegalArgumentException("이미 지원한 공고입니다.");
		}

		var targetResume = targetResumeRepository.findById(command.resumeId())
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
		return JobApplicationTargetResponse.from(jobApplicationRepository.save(application));
	}

	@Override
	@Transactional
	public void cancelApplication(Long jobId, String username) {
		var user = userRepository.findWithLockByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
		var application = jobApplicationRepository.findByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("취소할 지원 내역이 없거나 이미 취소한 지원입니다."));
		application.cancel();
	}
    @Override
    public MyJobApplicationResponse getMyApplication(Long jobId, String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        // 비공개 초안과 삭제된 공고는 공개 상세와 동일하게 조회하지 않는다.
        jobRepository.findPublicJobById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다."));
        return jobApplicationRepository.findByJobIdAndUserIdAndCanceledAtIsNull(jobId, user.getId())
                .map(MyJobApplicationResponse::from)
                .orElseGet(() -> MyJobApplicationResponse.notApplied(jobId));
    }

    private void validateAcceptingApplications(Job job) {
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new IllegalArgumentException("현재 지원을 받는 공고가 아닙니다.");
        }
        // 마감일 당일까지 접수 가능. JVM의 기본 시간대와 무관하게 한국 날짜를 사용한다.
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")))) {
            throw new IllegalArgumentException("접수 마감일이 지난 공고입니다.");
        }
    }
}
