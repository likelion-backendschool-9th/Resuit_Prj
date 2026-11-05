package com.newlecture.resuit.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import com.newlecture.resuit.dto.CreateJobRequest;
import com.newlecture.resuit.entity.JobQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlecture.resuit.dto.JobResponse;
import com.newlecture.resuit.dto.JobDetailResponse;
import com.newlecture.resuit.dto.GeneralJobApplicationRequest;
import com.newlecture.resuit.dto.JobApplicationResponse;
import com.newlecture.resuit.dto.SpecialJobApplicationRequest;
import com.newlecture.resuit.entity.JobApplication;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.repository.TargetResumeRepository;
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
	private final TargetResumeRepository targetResumeRepository;

    @Override
    @Transactional
    public JobDetailResponse createJob(CreateJobRequest request, String username) {
        var company = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("기업 계정이 필요합니다."));
        if (!"COMPANY".equals(company.getRole())) {
            throw new AccessDeniedException("기업 계정만 공고를 작성할 수 있습니다.");
        }

        String type = request.type().toUpperCase(Locale.ROOT);
        List<CreateJobRequest.Question> questions = request.questions() == null ? List.of() : request.questions();
        if ("GENERAL".equals(type) && !questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일반 공고에는 요구 문항을 등록할 수 없습니다.");
        }
        if ("SPECIAL".equals(type) && questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "특화 공고에는 요구 문항이 필요합니다.");
        }

        var job = jobRepository.save(Job.builder()
                .company(company)
                .type(type)
                .title(request.title().trim())
                .location(request.location())
                .employmentType(request.employmentType())
                .career(request.career())
                .description(request.description())
                .requirements(request.requirements())
                .preferred(request.preferred())
                .deadline(request.deadline())
                .status("OPEN")
                .build());

        List<JobQuestion> entities = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            var question = questions.get(index);
            entities.add(JobQuestion.builder()
                    .job(job)
                    .content(question.content().trim())
                    .charLimit(question.charLimit())
                    .orderNo(index + 1)
                    .build());
        }
        var savedQuestions = jobQuestionRepository.saveAll(entities).stream()
                .map(question -> new JobDetailResponse.Question(question.getId(), question.getContent(),
                        question.getCharLimit(), question.getOrderNo()))
                .toList();
        return JobDetailResponse.from(job, savedQuestions);
    }

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
