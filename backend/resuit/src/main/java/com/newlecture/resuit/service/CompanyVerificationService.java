package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.CompanyVerificationDto.EmailRequest;
import com.newlecture.resuit.dto.CompanyVerificationDto.EmailResponse;
import com.newlecture.resuit.dto.CompanyVerificationDto.MyStatusResponse;
import java.util.UUID;

public interface CompanyVerificationService {
    EmailResponse verify(String username, EmailRequest input);

    MyStatusResponse getMyStatus(UUID userId);
}
