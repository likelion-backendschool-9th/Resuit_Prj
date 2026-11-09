package com.newlecture.resuit.service;

import java.util.List;

import com.newlecture.resuit.dto.CompanyOfferCreateRequest;
import com.newlecture.resuit.dto.CompanyOfferReply;
import com.newlecture.resuit.dto.CompanyOfferResponse;

public interface CompanyOfferService {

    CompanyOfferResponse send(String username, CompanyOfferCreateRequest request);

    List<CompanyOfferResponse> getReceived(String username);

    List<CompanyOfferResponse> getSent(String username);

    // 받은 사람이 제안을 승낙하거나 거절한다. 결과는 보낸 기업의 보낸 제안 목록에서 보인다.
    CompanyOfferResponse respond(CompanyOfferReply reply);
}
