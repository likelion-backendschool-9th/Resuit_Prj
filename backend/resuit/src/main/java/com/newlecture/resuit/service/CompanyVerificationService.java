package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanyVerificationDto.ConfirmRequest;
import com.newlecture.resuit.dto.CompanyVerificationDto.ConfirmResponse;
import com.newlecture.resuit.dto.CompanyVerificationDto.EmailRequest;
import com.newlecture.resuit.dto.CompanyVerificationDto.EmailResponse;

public interface CompanyVerificationService {
    EmailResponse send(String username, EmailRequest input);

    ConfirmResponse confirm(String username, ConfirmRequest input);
}
