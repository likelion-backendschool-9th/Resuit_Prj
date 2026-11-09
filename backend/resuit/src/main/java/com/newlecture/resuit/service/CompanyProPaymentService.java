package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanyProPaymentCreateRequest;
import com.newlecture.resuit.dto.CompanyProPaymentOrderResponse;
import com.newlecture.resuit.dto.CompanyProPaymentResultResponse;
import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;

public interface CompanyProPaymentService {

    CompanyProPaymentOrderResponse create(String username, CompanyProPaymentCreateRequest request);

    CompanyProPaymentResultResponse confirm(String username, CreditPaymentConfirmRequest request);
}
