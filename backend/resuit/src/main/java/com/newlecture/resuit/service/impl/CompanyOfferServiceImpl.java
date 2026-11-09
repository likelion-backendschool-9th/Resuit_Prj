package com.newlecture.resuit.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.CompanyOfferCreateRequest;
import com.newlecture.resuit.dto.CompanyOfferResponse;
import com.newlecture.resuit.entity.CompanyOffer;
import com.newlecture.resuit.entity.Job;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.CompanyOfferRepository;
import com.newlecture.resuit.repository.JobRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanyOfferService;

@Service
public class CompanyOfferServiceImpl implements CompanyOfferService {

    // 처음 보낸 제안의 상태. 수락(ACCEPTED)·거절(DECLINED)은 아직 없다.
    private static final String SENT = "SENT";
    private static final String DRAFT = "DRAFT";

    private final CompanyOfferRepository offers;
    private final UserRepository users;
    private final JobRepository jobs;
    private final CommonResumeRepository resumes;
    private final CompanyMemberGuard companyGuard;

    public CompanyOfferServiceImpl(CompanyOfferRepository offers, UserRepository users, JobRepository jobs,
            CommonResumeRepository resumes, CompanyMemberGuard companyGuard) {
        this.offers = offers;
        this.users = users;
        this.jobs = jobs;
        this.resumes = resumes;
        this.companyGuard = companyGuard;
    }

    @Override
    @Transactional
    public CompanyOfferResponse send(String username, CompanyOfferCreateRequest request) {
        User company = companyGuard.requireCompany(username);
        if (company.getId().equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게는 채용 제안을 보낼 수 없습니다.");
        }
        // 받는 사람은 인재 찾기에 보이는 회원(공개 이력서가 있는 회원)이어야 한다. 아니면 없는 회원처럼 404다.
        User target = users.findById(request.userId())
                .filter(user -> resumes.existsByUserIdAndIsPublicTrue(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제안을 보낼 회원을 찾을 수 없습니다."));
        // 공고는 선택이다. 넣었으면 내 회사의, 삭제되지 않은, 임시 저장이 아닌 공고여야 한다.
        Job job = request.jobId() == null ? null : findMyJob(request.jobId(), company);
        CompanyOffer offer = offers.save(CompanyOffer.builder()
                .company(company)
                .user(target)
                .job(job)
                .message(request.message().trim())
                .status(SENT)
                .build());
        return CompanyOfferResponse.from(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyOfferResponse> getReceived(String username) {
        User me = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return offers.findByUser_IdOrderByIdDesc(me.getId()).stream()
                .map(CompanyOfferResponse::from)
                .toList();
    }

    // 우리 회사가 보낸 제안을 최신순으로 돌려준다. 기업 회원만 볼 수 있다.
    @Override
    @Transactional(readOnly = true)
    public List<CompanyOfferResponse> getSent(String username) {
        User company = companyGuard.requireCompany(username);
        // 받은 회원·공고를 함께 읽어 응답 DTO로 바꾼다. 받은 제안과 같은 모양이라 DTO를 재사용한다.
        return offers.findByCompany_IdOrderByIdDesc(company.getId()).stream()
                .map(CompanyOfferResponse::from)
                .toList();
    }

    // 내 회사의 공개된 공고를 찾는다. 남의 공고, 삭제된 공고, 임시 저장 공고는 없는 공고처럼 404다.
    private Job findMyJob(Long jobId, User company) {
        return jobs.findByIdAndDeletedAtIsNull(jobId)
                .filter(job -> job.getCompany().getId().equals(company.getId()))
                // 임시 저장(DRAFT) 공고는 제외한다. 공고 코드가 대소문자를 섞어 쓸 수 있어 무시하고 비교한다.
                .filter(job -> !DRAFT.equalsIgnoreCase(job.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "함께 보낼 공고를 찾을 수 없습니다."));
    }
}
