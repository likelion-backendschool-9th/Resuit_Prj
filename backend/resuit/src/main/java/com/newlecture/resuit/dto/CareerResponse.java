package com.newlecture.resuit.dto;

import java.time.LocalDate;

public record CareerResponse(Long id, String companyName, String jobTitle, LocalDate startDate,
        LocalDate endDate, String description, int displayOrder) {
}
