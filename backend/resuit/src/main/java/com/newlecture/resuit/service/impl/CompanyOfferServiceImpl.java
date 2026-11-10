package com.newlecture.resuit.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.CompanyOfferCreateRequest;
import com.newlecture.resuit.dto.CompanyOfferReply;
import com.newlecture.resuit.dto.CompanyOfferResponse;
import com.newlecture.resuit.dto.notification.NotificationCommand;
import com.newlecture.resuit.entity.CompanyOffer;
import com.newlecture.resuit.entity.Job;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CommonResumeRepository;
import com.newlecture.resuit.repository.CompanyOfferRepository;
import com.newlecture.resuit.repository.JobRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanyOfferService;
import com.newlecture.resuit.service.NotificationService;
import com.newlecture.resuit.service.NotificationType;

@Service
public class CompanyOfferServiceImpl implements CompanyOfferService {

    // 처음 보낸 제안의 상태. 받은 사람이 응답하면 ACCEPTED(승낙)나 DECLINED(거절)가 된다.
    private static final String SENT = "SENT";
    private static final String DRAFT = "DRAFT";

    private final CompanyOfferRepository offers;
    private final UserRepository users;
    private final JobRepository jobs;
    private final CommonResumeRepository resumes;
    private final CompanyMemberGuard companyGuard;
    private final NotificationService notifications;

    public CompanyOfferServiceImpl(CompanyOfferRepository offers, UserRepository users, JobRepository jobs,
            CommonResumeRepository resumes, CompanyMemberGuard companyGuard, NotificationService notifications) {
        this.offers = offers;
        this.users = users;
        this.jobs = jobs;
        this.resumes = resumes;
        this.companyGuard = companyGuard;
        this.notifications = notifications;
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
        // 같은 회사가 동시에 두 번 보내도 한 건만 저장되도록 기업 회원 행을 잠그고 중복을 확인한다.
        users.findWithLockById(company.getId());
        // 같은 회원에게는 제안을 한 번만 보낼 수 있다. 응답(승낙·거절)과 상관없이 다시 보낼 수 없다.
        if (offers.existsByCompany_IdAndUser_Id(company.getId(), target.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 이 회원에게 채용 제안을 보냈습니다.");
        }
        // 공고는 선택이다. 넣었으면 내 회사의, 삭제되지 않은, 임시 저장이 아닌 공고여야 한다.
        Job job = request.jobId() == null ? null : findMyJob(request.jobId(), company);
        CompanyOffer offer = offers.save(CompanyOffer.builder()
                .company(company)
                .user(target)
                .job(job)
                .message(request.message().trim())
                .status(SENT)
                .build());
        // 받는 회원에게 제안이 왔다고 알린다.
        notifications.notify(new NotificationCommand(target.getId(), company.getId(), NotificationType.OFFER_RECEIVED,
                NotificationService.nameOf(company) + " 님이 채용 제안을 보냈어요.", "/offers"));
        return toResponses(List.of(offer)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyOfferResponse> getReceived(String username) {
        User me = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return toResponses(offers.findByUser_IdOrderByIdDesc(me.getId()));
    }

    // 우리 회사가 보낸 제안을 최신순으로 돌려준다. 기업 회원만 볼 수 있다.
    @Override
    @Transactional(readOnly = true)
    public List<CompanyOfferResponse> getSent(String username) {
        User company = companyGuard.requireCompany(username);
        // 받은 회원·공고를 함께 읽어 응답 DTO로 바꾼다. 받은 제안과 같은 모양이라 DTO를 재사용한다.
        return toResponses(offers.findByCompany_IdOrderByIdDesc(company.getId()));
    }

    // 받은 제안에 응답한다. 받은 사람만, 아직 응답하지 않은(SENT) 제안에만 한 번 응답할 수 있다.
    @Override
    @Transactional
    public CompanyOfferResponse respond(CompanyOfferReply reply) {
        User me = users.findByUsername(reply.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        // 내가 받은 제안이 아니면 없는 제안처럼 404다.
        CompanyOffer offer = offers.findByIdAndUser_Id(reply.offerId(), me.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제안을 찾을 수 없습니다."));
        // 이미 승낙·거절한 제안은 바꿀 수 없다.
        if (!SENT.equals(offer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 응답한 제안입니다.");
        }
        offer.respond(reply.status());
        // 제안을 보낸 기업에게 응답을 알린다.
        notifications.notify(new NotificationCommand(offer.getCompany().getId(), me.getId(), NotificationType.OFFER_RESPONDED,
                NotificationService.nameOf(me) + " 님이 채용 제안을 " + ("ACCEPTED".equals(offer.getStatus()) ? "승낙" : "거절") + "했어요.",
                "/company/offers"));
        return toResponses(List.of(offer)).get(0);
    }

    // 제안 목록을 응답으로 바꾼다. 기업 이메일은 기업 회원이 가입할 때 입력한 이메일이다.
    private List<CompanyOfferResponse> toResponses(List<CompanyOffer> list) {
        return list.stream()
                .map(offer -> CompanyOfferResponse.from(offer, offer.getCompany().getEmail()))
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
