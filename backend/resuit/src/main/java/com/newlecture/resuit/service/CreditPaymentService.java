package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;
import com.newlecture.resuit.dto.CreditPaymentCreateRequest;
import com.newlecture.resuit.dto.CreditPaymentOrderResponse;
import com.newlecture.resuit.dto.CreditPaymentResultResponse;

public interface CreditPaymentService {

    CreditPaymentOrderResponse create(String username, CreditPaymentCreateRequest request);

    CreditPaymentResultResponse confirm(String username, CreditPaymentConfirmRequest request);
}
