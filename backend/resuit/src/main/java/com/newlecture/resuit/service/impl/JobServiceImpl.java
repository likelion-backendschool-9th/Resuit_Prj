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
	private final UserRepository userRepository;

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
    @Transactional
    public JobDetailResponse updateJob(Long jobId, CreateJobRequest request, String username) {
        var company = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("기업 계정이 필요합니다."));
        if (!"COMPANY".equals(company.getRole())) {
            throw new AccessDeniedException("기업 계정만 공고를 수정할 수 있습니다.");
        }

        var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobId));
        if (!job.getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("본인이 작성한 공고만 수정할 수 있습니다.");
        }

        String type = request.type().toUpperCase(Locale.ROOT);
        List<CreateJobRequest.Question> questions = request.questions() == null ? List.of() : request.questions();
        validateQuestions(type, questions);

        job.update(
                type,
                request.title().trim(),
                request.location(),
                request.employmentType(),
                request.career(),
                request.description(),
                request.requirements(),
                request.preferred(),
                request.deadline());

        jobQuestionRepository.deleteByJob_Id(jobId);
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
		String filterType = type == null || type.isBlank() ? null : type.trim();
		Page<Job> jobs = jobRepository.findPublicJobs(filterType, pageable);
		return jobs.map(JobResponse::from);
	}

	@Override
	public JobDetailResponse getJob(Long id) {
		var job = jobRepository.findPublicJobById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다: " + id));
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
    public void deleteJob(Long jobId, String username) {
        var company = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("기업 계정이 필요합니다."));
        if (!"COMPANY".equals(company.getRole())) {
            throw new AccessDeniedException("기업 계정만 공고를 삭제할 수 있습니다.");
        }

        var job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다: " + jobId));
        if (!job.getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("본인이 작성한 공고만 삭제할 수 있습니다.");
        }
        job.delete();
    }

    private void validateQuestions(String type, List<CreateJobRequest.Question> questions) {
        if ("GENERAL".equals(type) && !questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일반 공고에는 요구 문항을 등록할 수 없습니다.");
        }
        if ("SPECIAL".equals(type) && questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "특화 공고에는 요구 문항이 필요합니다.");
        }
    }

}
