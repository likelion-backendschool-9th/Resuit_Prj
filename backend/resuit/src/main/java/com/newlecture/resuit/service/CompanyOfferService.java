package com.newlecture.resuit.service;

import java.util.List;

import com.newlecture.resuit.dto.CompanyOfferCreateRequest;
import com.newlecture.resuit.dto.CompanyOfferResponse;

public interface CompanyOfferService {

    CompanyOfferResponse send(String username, CompanyOfferCreateRequest request);

    List<CompanyOfferResponse> getReceived(String username);
}
